package br.com.fiap.restauranteapi.repository;

import br.com.fiap.restauranteapi.domain.User;
import br.com.fiap.restauranteapi.domain.UserType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = this::map;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = ?", rowMapper, id).stream().findFirst();
    }

    public Optional<User> findByEmailIgnoreCase(String email) {
        return jdbcTemplate.query("SELECT * FROM users WHERE LOWER(email) = LOWER(?)", rowMapper, email).stream().findFirst();
    }

    public Optional<User> findByLoginIgnoreCase(String login) {
        return jdbcTemplate.query("SELECT * FROM users WHERE LOWER(login) = LOWER(?)", rowMapper, login).stream().findFirst();
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY id", rowMapper);
    }

    public List<User> findByNameContainingIgnoreCase(String name) {
        return jdbcTemplate.query("SELECT * FROM users WHERE LOWER(name) LIKE LOWER(?) ORDER BY name", rowMapper, "%" + name + "%");
    }

    public User save(User user) {
        user.setLastModifiedAt(OffsetDateTime.now());
        if (user.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var statement = connection.prepareStatement("""
                        INSERT INTO users (name, email, login, password_hash, address, type, last_modified_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """, new String[]{"id"});
                statement.setString(1, user.getName());
                statement.setString(2, user.getEmail());
                statement.setString(3, user.getLogin());
                statement.setString(4, user.getPasswordHash());
                statement.setString(5, user.getAddress());
                statement.setString(6, user.getType().name());
                statement.setTimestamp(7, Timestamp.valueOf(user.getLastModifiedAt().toLocalDateTime()));
                return statement;
            }, keyHolder);
            user.setId(keyHolder.getKey().longValue());
        } else {
            jdbcTemplate.update("""
                    UPDATE users
                    SET name = ?, email = ?, login = ?, password_hash = ?, address = ?, type = ?, last_modified_at = ?
                    WHERE id = ?
                    """,
                    user.getName(),
                    user.getEmail(),
                    user.getLogin(),
                    user.getPasswordHash(),
                    user.getAddress(),
                    user.getType().name(),
                    Timestamp.valueOf(user.getLastModifiedAt().toLocalDateTime()),
                    user.getId()
            );
        }
        return user;
    }

    public void delete(User user) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", user.getId());
    }

    private User map(ResultSet resultSet, int rowNumber) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setAddress(resultSet.getString("address"));
        user.setType(mapUserType(resultSet.getString("type")));
        Timestamp lastModifiedAt = resultSet.getTimestamp("last_modified_at");
        user.setLastModifiedAt(lastModifiedAt == null
                ? OffsetDateTime.now()
                : lastModifiedAt.toLocalDateTime().atOffset(OffsetDateTime.now().getOffset()));
        return user;
    }

    private UserType mapUserType(String type) {
        if ("1".equals(type)) {
            return UserType.CLIENTE;
        }
        if ("2".equals(type)) {
            return UserType.DONO_RESTAURANTE;
        }
        return UserType.valueOf(type);
    }
}
