
from pathlib import Path
from datetime import datetime
from zoneinfo import ZoneInfo
from collections import Counter
import html
import json
import re
import textwrap

from reportlab.pdfgen import canvas
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak,
    Preformatted, Flowable, KeepTogether,
)
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "output" / "pdf"
OUT.mkdir(parents=True, exist_ok=True)
DATA = json.loads((ROOT / "output/data/relatorio-semanal.json").read_text(encoding="utf-8-sig"))
ORIGIN = json.loads((ROOT / "output/data/origem-relatorio.json").read_text(encoding="utf-8-sig"))
ZONE = ZoneInfo("America/Sao_Paulo")
NAVY = colors.HexColor("#14324B")
TEAL = colors.HexColor("#067D85")
LIGHT = colors.HexColor("#EEF5F8")
GRAY = colors.HexColor("#546575")
RED = colors.HexColor("#B84646")
AMBER = colors.HexColor("#B57B19")
GREEN = colors.HexColor("#34845A")
W, H = A4
BODY = W - 84

for name, file in [("Arial", "arial.ttf"), ("Arial-Bold", "arialbd.ttf"),
                   ("Arial-Italic", "ariali.ttf"), ("Arial-BoldItalic", "arialbi.ttf")]:
    pdfmetrics.registerFont(TTFont(name, str(Path("C:/Windows/Fonts") / file)))
pdfmetrics.registerFontFamily("Arial", normal="Arial", bold="Arial-Bold", italic="Arial-Italic", boldItalic="Arial-BoldItalic")
styles = getSampleStyleSheet()
styles.add(ParagraphStyle("BodyA", fontName="Arial", fontSize=9.4, leading=13.7, textColor=NAVY, spaceAfter=8))
styles.add(ParagraphStyle("TitleA", fontName="Arial-Bold", fontSize=24, leading=29, textColor=NAVY, spaceAfter=15))
styles.add(ParagraphStyle("H2A", fontName="Arial-Bold", fontSize=18, leading=23, textColor=NAVY, spaceAfter=13, keepWithNext=True))
styles.add(ParagraphStyle("H3A", fontName="Arial-Bold", fontSize=11.2, leading=15, textColor=TEAL, spaceBefore=7, spaceAfter=7, keepWithNext=True))
styles.add(ParagraphStyle("QuoteA", parent=styles["BodyA"], fontName="Arial-Italic", fontSize=9.6, leading=14.4, spaceAfter=0))
styles.add(ParagraphStyle("BulletA", parent=styles["BodyA"], leftIndent=12, firstLineIndent=0, bulletIndent=0, spaceAfter=5))
styles.add(ParagraphStyle("SmallA", parent=styles["BodyA"], fontSize=8, leading=11.3, textColor=GRAY, spaceAfter=6))
styles.add(ParagraphStyle("CellA", fontName="Arial", fontSize=8.1, leading=11.3, textColor=NAVY))
styles.add(ParagraphStyle("CellHeadA", parent=styles["CellA"], fontName="Arial-Bold", textColor=colors.white))
styles.add(ParagraphStyle("CodeA", fontName="Courier", fontSize=7.7, leading=10.8, textColor=NAVY, leftIndent=8, rightIndent=8, spaceBefore=3, spaceAfter=9, backColor=LIGHT, borderPadding=8))


def markup(text):
    text = html.escape(text)
    text = re.sub(r"\[([^\]]+)\]\((https?://[^\s)]+)\)", r'<link href="\2" color="#067D85">\1</link>', text)
    text = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", text)
    text = re.sub(r"`([^`]+)`", r'<font name="Courier">\1</font>', text)
    return text


def para(text, style="BodyA"):
    return Paragraph(markup(text), styles[style])


def table(rows, widths=None):
    if widths is None:
        widths = [BODY * .28, BODY * .72] if len(rows[0]) == 2 else [BODY * .24, BODY * .25, BODY * .51]
    data = [[para(cell, "CellHeadA" if i == 0 else "CellA") for cell in row] for i, row in enumerate(rows)]
    t = Table(data, colWidths=widths, repeatRows=1, hAlign="LEFT")
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, LIGHT]),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 8),
        ("RIGHTPADDING", (0, 0), (-1, -1), 8),
        ("TOPPADDING", (0, 0), (-1, -1), 7),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
        ("LINEBELOW", (0, 0), (-1, 0), .5, NAVY),
    ]))
    return t


class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.states = []
    def showPage(self):
        self.states.append(dict(self.__dict__))
        self._startPage()
    def save(self):
        count = len(self.states)
        for state in self.states:
            self.__dict__.update(state)
            self.setStrokeColor(colors.HexColor("#CCD9E1"))
            self.line(42, 39, W - 42, 39)
            self.setFont("Arial", 7.5)
            self.setFillColor(GRAY)
            self.drawString(42, 26, "TECH04 | Material acadêmico | Dados fictícios")
            self.drawRightString(W - 42, 26, f"{self._pageNumber} / {count}")
            super().showPage()
        super().save()


def header(c, doc):
    c.saveState()
    c.setFillColor(TEAL)
    c.rect(0, H - 8, W, 8, fill=True, stroke=False)
    c.setFont("Arial-Bold", 7.5)
    c.setFillColor(GRAY)
    c.drawString(42, H - 30, "FIAP | TECH CHALLENGE - FASE 4")
    c.drawRightString(W - 42, H - 30, "Azure Functions | Java 21")
    c.restoreState()


def build(path, story, title):
    doc = SimpleDocTemplate(str(path), pagesize=A4, rightMargin=42, leftMargin=42,
                            topMargin=49, bottomMargin=53, title=title,
                            author="Projeto acadêmico Tech04", pageCompression=1)
    doc.build(story, onFirstPage=header, onLaterPages=header, canvasmaker=NumberedCanvas)


class Chart(Flowable):
    def __init__(self, values, labels, title, color=TEAL, height=125):
        super().__init__()
        self.values, self.labels, self.title, self.color = values, labels, title, color
        self.width, self.height = BODY, height
    def draw(self):
        c = self.canv
        c.setFillColor(NAVY)
        c.setFont("Arial-Bold", 10)
        c.drawString(0, self.height - 13, self.title)
        base = 25
        maxv = max(self.values, default=1) or 1
        step = self.width / len(self.values)
        for i, (value, label) in enumerate(zip(self.values, self.labels)):
            x = i * step + step * .23
            h = (self.height - 55) * value / maxv
            c.setFillColor(self.color[i] if isinstance(self.color, list) else self.color)
            if value:
                c.roundRect(x, base, step * .54, h, 3, fill=True, stroke=False)
            else:
                c.setStrokeColor(colors.HexColor('#B9C8D2'))
                c.line(x, base, x + step * .54, base)
            c.setFont("Arial-Bold", 9)
            c.setFillColor(NAVY)
            c.drawCentredString(x + step * .27, base + h + 7, str(value))
            c.setFont("Arial", 8)
            c.drawCentredString(x + step * .27, 10, label)


class Architecture(Flowable):
    def __init__(self):
        super().__init__()
        self.width, self.height = BODY, 176
    def draw(self):
        c = self.canv
        box_width = 143
        gap = (BODY - 3 * box_width) / 2
        rows = [('HTTP POST', 'ReceberAvaliacao', 'Table Storage'),
                ('Fila de críticas', 'EnviarAlerta', 'E-mail / ACS'),
                ('Timer semanal', 'GerarRelatorioSemanal', 'Blob privado')]
        for i, labels in enumerate(rows):
            y = self.height - 39 - i * 48
            for j, label in enumerate(labels):
                x = j * (box_width + gap)
                c.setFillColor(LIGHT if j != 1 else NAVY)
                c.roundRect(x, y, box_width, 31, 5, fill=True, stroke=False)
                c.setFillColor(NAVY if j != 1 else colors.white)
                c.setFont('Arial-Bold', 8.4)
                c.drawCentredString(x + box_width / 2, y + 12, label)
                if j < 2:
                    start, end, ay = x + box_width + 4, x + box_width + gap - 4, y + 15
                    c.setStrokeColor(TEAL)
                    c.setLineWidth(1.2)
                    c.line(start, ay, end, ay)
                    c.line(end - 4, ay + 3, end, ay)
                    c.line(end - 4, ay - 3, end, ay)
        c.setFillColor(GRAY)
        c.setFont('Arial', 8)
        c.drawString(0, 6, 'Se crítica, a função de entrada publica na fila; o e-mail é processado separadamente.')


def parse_md(text):
    story = []
    lines = text.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        if not line:
            i += 1
            continue
        if line == "<!-- pagebreak -->":
            story.append(PageBreak()); i += 1; continue
        if line == "<!-- architecture -->":
            story.append(Architecture()); story.append(Spacer(1, 4)); i += 1; continue
        if line.startswith("```"):
            i += 1
            code = []
            while i < len(lines) and not lines[i].strip().startswith("```"):
                code.extend(textwrap.wrap(lines[i], width=100, subsequent_indent="    ", replace_whitespace=False, drop_whitespace=False) or [""])
                i += 1
            story.append(Preformatted("\n".join(code), styles["CodeA"]))
            i += 1; continue
        if line.startswith("|"):
            rows = []
            while i < len(lines) and lines[i].strip().startswith("|"):
                cells = [x.strip() for x in lines[i].strip().strip("|").split("|")]
                if not all(re.fullmatch(r"[:\- ]+", x) for x in cells): rows.append(cells)
                i += 1
            story.append(table(rows)); story.append(Spacer(1, 9)); continue
        if line.startswith("### "):
            story.append(para(line[4:], "H3A")); i += 1; continue
        if line.startswith("## "):
            story.append(para(line[3:], "H2A")); i += 1; continue
        if line.startswith("# "):
            story.append(para(line[2:], "TitleA")); i += 1; continue
        if line.startswith("> "):
            q = Table([[para(line[2:], "QuoteA")]], colWidths=[BODY])
            q.setStyle(TableStyle([("BACKGROUND",(0,0),(-1,-1),LIGHT), ("BOX",(0,0),(-1,-1),.4,colors.HexColor("#BFD6E0")), ("LEFTPADDING",(0,0),(-1,-1),12), ("RIGHTPADDING",(0,0),(-1,-1),12), ("TOPPADDING",(0,0),(-1,-1),10), ("BOTTOMPADDING",(0,0),(-1,-1),10)]))
            story.extend([q, Spacer(1, 10)]); i += 1; continue
        if line.startswith("- "):
            story.append(Paragraph(markup(line[2:]), styles["BulletA"], bulletText="•")); i += 1; continue
        numbered = re.match(r"^(\d+\.) (.*)", line)
        if numbered:
            story.append(Paragraph(markup(numbered.group(2)), styles["BulletA"], bulletText=numbered.group(1))); i += 1; continue
        story.append(para(line)); i += 1
    return story


def verify_report():
    records = DATA["avaliacoes"]
    assert len(records) == DATA["quantidade"]
    assert abs(sum(r["nota"] for r in records) / len(records) - DATA["mediaNotas"]) < 1e-9
    actual_urgency = Counter(r["urgencia"] for r in records)
    assert dict(actual_urgency) == DATA["quantidadePorUrgencia"]
    actual_days = Counter(datetime.fromisoformat(r["dataEnvio"].replace("Z", "+00:00")).astimezone(ZONE).date().isoformat() for r in records)
    assert all(actual_days[d] == n for d, n in DATA["quantidadePorDia"].items())
    assert DATA["quantidade"] == 12 and DATA["mediaNotas"] == 6
    assert all(r["id"].startswith("demo-20261019-") and r["descricao"].startswith("DEMO -") for r in records)


def report_pdf():
    start = datetime.fromisoformat(DATA["inicioInclusivo"].replace("Z", "+00:00")).astimezone(ZONE)
    end = datetime.fromisoformat(DATA["fimExclusivo"].replace("Z", "+00:00")).astimezone(ZONE)
    n = DATA["quantidade"]
    s = [para("Relatório semanal de feedbacks", "TitleA"),
         para("Semana de 19 a 25 de outubro de 2026", "H2A"),
         para("Demonstração acadêmica", "H3A"),
         para("O PDF organiza os mesmos dados do JSON e acrescenta explicações para estudo; não foi gerado automaticamente pela função em cloud."),
         table([["Avaliações", "Média das notas", "Notas somadas"], [str(n), "6,0 / 10", "72"]], [BODY/3]*3),
         Spacer(1, 15),
         Chart(list(DATA["quantidadePorDia"].values()), ["Seg 19", "Ter 20", "Qua 21", "Qui 22", "Sex 23", "Sáb 24", "Dom 25"], "Quantidade de avaliações por dia", height=130),
         Spacer(1, 9),
         table([["Urgência", "Quantidade", "Participação"], ["CRITICA (0 a 3)", "3", "25%"], ["ATENCAO (4 a 6)", "3", "25%"], ["NORMAL (7 a 10)", "6", "50%"]]),
         Spacer(1, 11),
         para("**Leitura do cenário:** metade das avaliações está na faixa normal. Três registros críticos ilustram dificuldades com deploy, permissões e acesso. São exemplos construídos para validar o sistema; não medem a satisfação real de alunos."),
         PageBreak(),
         para("Avaliações que compõem o relatório", "H2A"),
         para("Datas e horas convertidas para America/Sao_Paulo."),
    ]
    rows = [["Data / hora", "Nota", "Urgência", "Descrição fictícia"]]
    for r in sorted(DATA["avaliacoes"], key=lambda x: x["dataEnvio"]):
        dt = datetime.fromisoformat(r["dataEnvio"].replace("Z", "+00:00")).astimezone(ZONE)
        rows.append([dt.strftime("%d/%m %H:%M"), str(r["nota"]), r["urgencia"], r["descricao"].removeprefix("DEMO - ")])
    s.extend([table(rows, [76, 42, 68, BODY - 186]), Spacer(1, 14),
              para("**Conferência:** 2 + 8 + 3 + 7 + 5 + 10 + 6 + 9 + 1 + 8 + 4 + 9 = 72. Média aritmética: 72 / 12 = 6,0. O domingo sem avaliações aparece com contagem zero e não adiciona uma nota zero ao cálculo."),
              PageBreak(), para("Origem, método e interpretação", "H2A"),
              para("Como este resultado foi produzido", "H3A"),
              para("Foram inseridos 12 registros de demonstração na tabela avaliacoes, com datas históricas fictícias de 19 a 25/10/2026. A inserção foi administrativa e idempotente por ID de demonstração: repetir a preparação na mesma semana atualiza os mesmos registros. Nenhum dado existente foi apagado."),
              para("A função semanal foi acionada manualmente pela API administrativa de Azure Functions, sem mudar o timer de produção acadêmica. Ela consultou os dados e escreveu o JSON no container privado relatorios. Os registros foram inseridos diretamente na tabela e não dispararam novos alertas por e-mail."),
              table([["Campo", "Valor verificado"], ["Conta", ORIGIN["storageAccount"]], ["Container / blob", "relatorios / " + ORIGIN["blob"]], ["Início inclusivo UTC", DATA["inicioInclusivo"]], ["Fim exclusivo UTC", DATA["fimExclusivo"]], ["Fuso do período", DATA["fusoHorario"]], ["Exportação", "output/data/relatorio-semanal.json"]]),
              Spacer(1, 13), para("Interpretação para a apresentação", "H3A"),
              para("A quarta-feira concentra três avaliações, o maior volume diário desta amostra. Três casos críticos representam 25% dos registros. Em um caso real, isso orientaria a revisão dos conteúdos com dificuldades, sem dispensar a leitura das descrições e investigação do contexto."),
              para("A média geral é calculada sobre as 12 notas, e não sobre sete médias diárias. Neste projeto, a API registra o instante de envio e o relatório inclui somente registros no intervalo da semana anterior, no fuso de São Paulo."),
              para("Como repetir em outra semana", "H3A"),
              Preformatted("./scripts/prepare-demo-report.ps1\n./scripts/download-report.ps1", styles["CodeA"]),
              para("A função e os scripts calculam o período dinamicamente. Registros adicionais podem alterar os totais. Este PDF é uma fotografia do resultado validado; futuras execuções do JSON não atualizam automaticamente este arquivo. O guia do vídeo explica como mostrar os novos valores."),
              para("Evidência complementar: alerta fictício da API já recebido e lido pelo destinatário, conforme confirmação do aluno. O relatório semanal e o fluxo de e-mail são verificações separadas.", "SmallA")])
    build(OUT / "relatorio-semanal-feedbacks.pdf", s, "Tech04 - Relatório semanal de feedbacks")


if __name__ == "__main__":
    verify_report()
    report_pdf()
    guide = (ROOT / "docs/roteiro-video.md").read_text(encoding="utf-8")
    build(OUT / "guia-estudo-roteiro-video.pdf", parse_md(guide), "Tech04 - Guia de estudo e roteiro do vídeo")
    print("Relatório e guia gerados com dados conferidos.")
