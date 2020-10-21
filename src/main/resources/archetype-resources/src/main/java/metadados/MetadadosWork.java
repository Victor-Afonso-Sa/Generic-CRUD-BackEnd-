package ${package}.metadados;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.jdbc.ReturningWork;

public class MetadadosWork  implements ReturningWork<Map<String, HashSet<String>>>{
	@Override
	public Map<String, HashSet<String>> execute(Connection connection) throws SQLException {
		Map<String, HashSet<String>> tabelas = new HashMap<String, HashSet<String>>();
		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		while (rs.next()) {
			tabelas.put(rs.getString("TABLE_NAME"), new HashSet<String>());
		}
		ResultSet rsColumn = md.getColumns(null, null, "%", null);
		while (rsColumn.next()) {
			Set<String> colunas = tabelas.get(rsColumn.getString("TABLE_NAME"));
			if (colunas != null) {
				colunas.add(rsColumn.getString("COLUMN_NAME"));
			}
		}
		return tabelas;
	}
}
