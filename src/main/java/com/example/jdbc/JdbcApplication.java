package com.example.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@SpringBootApplication
public class JdbcApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(JdbcApplication.class, args);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	DataSource dataSource;


	@Override
	public void run(String... args) {
		// Custom Exception Handling
		jdbcTemplate.setExceptionTranslator(new CustomSQLErrorCodeTranslator());

		// 1. Simple Inserts
		jdbcTemplate.execute("INSERT INTO PERSON(first_name, last_name) VALUES('Victor', 'Hugo')");
		jdbcTemplate.update("INSERT INTO PERSON(first_name, last_name) VALUES (?, ?)", "Bill", "Gates");

		// 2. Simple Query
		int result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON", Integer.class);
		System.out.println("2. Number of Persons, " + result);

		// 3. Named Parameter Query
		SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", 1);
		String firstName = namedParameterJdbcTemplate.queryForObject("SELECT FIRST_NAME FROM PERSON WHERE ID = :id", namedParameters, String.class);
		System.out.println("3. Person with ID=1 has name=" + firstName);

		// 4. RowMapper
		String query = "SELECT * FROM PERSON WHERE ID = ?";
		Person person = jdbcTemplate.queryForObject(query, new PersonRowMapper(), 1);
		System.out.println("4. " + person.toString());

		// 5. SimpleJDBC
		SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("PERSON");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("ID", 1000);
		parameters.put("FIRST_NAME", "Jesus");
		parameters.put("LAST_NAME", "Christ");
		int i = simpleJdbcInsert.execute(parameters);
		System.out.println("5. SimpleJDBC returned i=" + i);

		// 6. SimpleJDBC with Generated Key Columns
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("PERSON")
				.usingGeneratedKeyColumns("ID");
		parameters = new HashMap<>();
		parameters.put("FIRST_NAME", "Jesus");
		parameters.put("LAST_NAME", "Christ");
		Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
		System.out.println("6. SimpleJDBC with Generated Key Columns return id=" + id.longValue());

		// 7. Stored Procedure Calls
//		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("READ_EMPLOYEE");
//		SqlParameterSource in = new MapSqlParameterSource().addValue("in_id", id);
//		Map<String, Object> out = simpleJdbcCall.execute(in);
//		System.out.println("7. " + out);

		// 8. Batch JdbcTemplate
		List<Person> people = List.of(
				Person.builder().id(100L).firstName("Person100").lastName("Person100").build(),
				Person.builder().id(101L).firstName("Person101").lastName("Person101").build(),
				Person.builder().id(102L).firstName("Person102").lastName("Person102").build(),
				Person.builder().id(103L).firstName("Person103").lastName("Person103").build()
		);
		int[] batched = jdbcTemplate.batchUpdate("INSERT INTO PERSON VALUES (?, ?, ?)",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ps.setLong(1, people.get(i).getId());
						ps.setString(2, people.get(i).getFirstName());
						ps.setString(3, people.get(i).getLastName());
					}
					@Override
					public int getBatchSize() {
						return 4;
					}
				});
		System.out.println("8. " + Arrays.toString(batched));

		// 9. Batch NamedParameterJdbcTemplate
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(List.of(
				Person.builder().id(104L).firstName("Person104").lastName("Person104").build(),
				Person.builder().id(105L).firstName("Person105").lastName("Person105").build(),
				Person.builder().id(106L).firstName("Person106").lastName("Person106").build(),
				Person.builder().id(107L).firstName("Person107").lastName("Person107").build()
		).toArray());
		int[] updateCounts = namedParameterJdbcTemplate.batchUpdate("INSERT INTO PERSON VALUES (:id, :firstName, :lastName)", batch);
		System.out.println("9. " + Arrays.toString(updateCounts));
	}
}
