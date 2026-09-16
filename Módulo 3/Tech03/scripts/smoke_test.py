"""End-to-end check against the local Docker demo. Uses only Python's standard library.

Run: python scripts/smoke_test.py
Creates synthetic appointments and captures emails in local Mailpit.
"""
import base64
import datetime as dt
import json
import random
import time
import urllib.error
import urllib.request


def request(url, payload=None, credentials=None):
    headers = {"Content-Type": "application/json"}
    if credentials:
        token = base64.b64encode(":".join(credentials).encode()).decode()
        headers["Authorization"] = "Basic " + token
    data = json.dumps(payload).encode() if payload is not None else None
    try:
        with urllib.request.urlopen(urllib.request.Request(url, data=data, headers=headers), timeout=10) as response:
            return response.status, json.loads(response.read())
    except urllib.error.HTTPError as error:
        raw = error.read()
        return error.code, json.loads(raw) if raw else {}


def wait_for(check, description, timeout=60):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        try:
            value = check()
            if value:
                print("PASS", description, flush=True)
                return value
        except (OSError, ValueError):
            pass
        time.sleep(1)
    raise AssertionError("Timeout: " + description)


def graphql(query, variables=None, user="medico", password="medico1234", forbidden=False):
    status, body = request("http://localhost:8080/graphql", {"query": query, "variables": variables or {}}, (user, password))
    assert status == 200, (status, body)
    if forbidden:
        assert body["errors"][0]["extensions"]["classification"] == "FORBIDDEN", body
    else:
        assert not body.get("errors"), body
    return body.get("data")


def mail_for(appointment_id, fragment=None):
    _, body = request("http://localhost:8025/api/v1/messages")
    for message in body.get("messages", []):
        _, detail = request("http://localhost:8025/api/v1/message/" + message["ID"])
        text = detail.get("Text", "")
        if appointment_id in text and (fragment is None or fragment in text):
            return detail
    return None


def main():
    wait_for(lambda: request("http://localhost:8080/actuator/health/readiness")[0] == 200, "agendamento pronto")
    wait_for(lambda: request("http://localhost:8081/actuator/health/readiness")[0] == 200, "notificacoes prontas")
    status, _ = request("http://localhost:8080/graphql", {"query": "{history(patientId:\"pac-001\"){id}}"})
    assert status == 401
    print("PASS autenticacao obrigatoria", flush=True)

    start = dt.datetime.now(dt.timezone.utc).replace(microsecond=0) + dt.timedelta(days=random.randint(100, 2000), hours=random.randint(1, 12))
    data = {"patientId": "pac-001", "doctorId": "doc-001", "startsAt": start.isoformat().replace("+00:00", "Z"), "durationMinutes": 30, "reason": "Teste ficticio"}
    created = graphql("mutation($input:AppointmentInput!){createAppointment(input:$input){id version startsAt}}", {"input": data}, "enfermeiro", "enfermeiro1234!")["createAppointment"]
    appointment_id = created["id"]
    assert created["version"] == 1
    print("PASS criacao GraphQL com PostgreSQL", flush=True)
    confirmation = wait_for(lambda: mail_for(appointment_id), "outbox -> RabbitMQ -> inbox -> SMTP/Mailpit")
    assert confirmation["Subject"] == "Consulta registrada ou atualizada"

    graphql("query($id:ID!){appointment(id:$id){id}}", {"id": appointment_id}, "paciente2", "Paciente456", forbidden=True)
    graphql("mutation($id:ID!){updateClinicalNotes(id:$id,expectedVersion:1,notes:\"proibido\"){id}}", {"id": appointment_id}, "enfermeiro", "enfermeiro1234", forbidden=True)
    own = graphql("query($id:ID!){appointment(id:$id){id}}", {"id": appointment_id}, "paciente", "paciente1234")
    assert own["appointment"]["id"] == appointment_id
    print("PASS perfis e propriedade das consultas", flush=True)

    data["startsAt"] = (start + dt.timedelta(hours=2)).isoformat().replace("+00:00", "Z")
    edited = graphql("mutation($id:ID!,$input:AppointmentInput!){updateAppointment(id:$id,expectedVersion:1,input:$input){id version startsAt}}", {"id": appointment_id, "input": data})["updateAppointment"]
    assert edited["version"] == 2
    wait_for(lambda: mail_for(appointment_id, data["startsAt"]), "reagendamento recebido e email com novo horario")
    notes = graphql("mutation($id:ID!){updateClinicalNotes(id:$id,expectedVersion:2,notes:\"Evolucao ficticia\"){version clinicalNotes}}", {"id": appointment_id})["updateClinicalNotes"]
    assert notes["version"] == 3
    print("PASS edicao clinica por medico", flush=True)

    # Invalid messages must reach the configured DLQ instead of causing an infinite loop.
    _, queue_before = request("http://localhost:15672/api/queues/%2F/hospital.notifications.dlq", credentials=("tech03", "Tech03RabbitLocal!"))
    dead_letters_before = queue_before.get("messages", 0)
    status, result = request("http://localhost:15672/api/exchanges/%2F/hospital.appointments.v1/publish", {"properties": {"content_type": "application/json"}, "routing_key": "appointment.changed", "payload": "{}", "payload_encoding": "string"}, ("tech03", "Tech03RabbitLocal!"))
    assert status == 200 and result["routed"]
    wait_for(lambda: request("http://localhost:15672/api/queues/%2F/hospital.notifications.dlq", credentials=("tech03", "Tech03RabbitLocal!"))[1].get("messages", 0) > dead_letters_before, "mensagem invalida encaminhada a DLQ")
    print(json.dumps({"status": "PASS", "appointmentId": appointment_id, "startsAt": data["startsAt"]}), flush=True)


if __name__ == "__main__":
    main()
