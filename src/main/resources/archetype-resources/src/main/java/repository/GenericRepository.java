package ${package}.repository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import ${package}.metadados.MetadadosWork;


@Transactional
@Repository
public class GenericRepository {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	@PersistenceContext(unitName = "default")
	private EntityManager entityManager;

	public String getAll(String tabela) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(tabela);
		final Query query = entityManager.createNativeQuery(sql.toString(), Tuple.class);
		return tableData(query.getResultList());
	}

	public String getByParameter(String tabela, Map<String, String> parametros) {
		String params = extrairKey(parametros, false, false);
		Collection<String> values = parametros.values();
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(tabela).append(" WHERE 1=1 AND ").append(params);
		final Query query = entityManager.createNativeQuery(sql.toString(), Tuple.class);
		int i = 1;
		for (Iterator<String> iter = values.iterator(); iter.hasNext();) {
			query.setParameter(i, iter.next());
			i++;
		}
		return tableData(query.getResultList());
	}

	public int delete(String tabela, Map<String, String> parametros) {
		String params = extrairKey(parametros, false, false);
		Collection<String> values = parametros.values();
		final StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(tabela).append(" WHERE 1=1 AND ").append(params);
		final Query query = entityManager.createNativeQuery(sql.toString());
		int i = 1;
		for (Iterator<String> iter = values.iterator(); iter.hasNext();) {
			query.setParameter(i, iter.next());
			i++;
		}

		return query.executeUpdate();
	}

	public int update(String tabela, Map<String, String> parametros, Map<String, String> objeto) throws ParseException {
		boolean precisa = true;
		String params = extrairKey(parametros, false, false);
		String objKeys = extrairKey(objeto, false, true);
		String[] timeType = converterData(tabela, objeto);
		Collection<String> values = parametros.values();
		Collection<String> objValues = objeto.values();
		final StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(tabela).append(" SET ").append(objKeys).append(" where 1=1 AND ").append(params);
		final Query query = entityManager.createNativeQuery(sql.toString());
		int i = 1;
		for (Iterator<String> iter = objValues.iterator(); iter.hasNext();) {
			String valor = iter.next();
			if (valor == "null" || valor == null) {
				valor = "";
			}
			if (timeType.length > 0 && !valor.isEmpty()) {
				for (int j = 0; j < timeType.length; j++) {
					if (valor == objeto.get(timeType[j].toLowerCase())
							|| valor == objeto.get(timeType[j].toUpperCase())) {
						String patterEntrada = "yyyy-MM-dd";
						if (valor.length() > 10) {
							patterEntrada = "yyyy-MM-dd'T'HH:mm:ss";
						}
						try {
							DateFormat df = new SimpleDateFormat(patterEntrada);
							Date date = (Date) df.parse(valor);
							query.setParameter(i, date);
							precisa = false;
						} catch (ParseException e) {
							throw e;
						}
					}
				}
			}
			if (precisa) {
				query.setParameter(i, valor);
			}
			i++;
			precisa = true;
		}
		for (Iterator<String> iter = values.iterator(); iter.hasNext();) {
			query.setParameter(i, iter.next());
			i++;
		}
		return query.executeUpdate();
	}

	public int insert(String tabela, List<Map<String, String>> listaObj) throws ParseException {
		boolean precisa = true;
		int control = -1;
		for (Map<String, String> objeto : listaObj) {
			String[] timeType = converterData(tabela, objeto);
			String objKeys = extrairKey(objeto, true, true);
			Collection<String> values = objeto.values();
			String q = createQuestion(values.size());
			final StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO ").append(tabela).append("(").append(objKeys).append(")").append(" VALUES ")
					.append("(").append(q).append(")");
			final Query query = entityManager.createNativeQuery(sql.toString());
			int i = 1;
			for (Iterator<String> iter = values.iterator(); iter.hasNext();) {
				String valor = iter.next();
				if (valor == null) {
					valor = "";
				}
				if (timeType.length > 0 && !valor.isEmpty()) {
					for (int j = 0; j < timeType.length; j++) {
						if (valor == objeto.get(timeType[j].toLowerCase())
								|| valor == objeto.get(timeType[j].toUpperCase())) {
							String patterEntrada = "yyyy-MM-dd";
							if (valor.length() > 10) {
								patterEntrada = "yyyy-MM-dd'T'HH:mm:ss";
							}
							try {
								DateFormat df = new SimpleDateFormat(patterEntrada);
								Date date = (Date) df.parse(valor);
								query.setParameter(i, date);
								precisa = false;
							} catch (ParseException e) {
								throw e;
							}

						}
					}
				}
				if (precisa) {
					query.setParameter(i, valor);
				}
				i++;
				precisa = true;
			}
			control = query.executeUpdate();
			if (control == 0) {
				return control;
			}
		}
		return control;
	}

	private String extrairKey(Map<String, String> parametro, boolean insert, boolean virgula) {
		final StringBuilder params = new StringBuilder();
		List<String> keylist = new ArrayList();
		Set<String> keys = parametro.keySet();
		for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
			keylist.add(iter.next());
		}
		int l = keylist.size() - 1;
		for (int i = 0; i < keylist.size(); i++) {
			if (!insert) {
				if (i == l) {
					params.append(keylist.get(l)).append("= ?");
				} else {
					if (virgula) {
						params.append(keylist.get(i)).append("= ?, ");
					} else {
						params.append(keylist.get(i)).append("= ? AND ");
					}

				}
			} else {
				if (i == l) {
					params.append(keylist.get(l));
				} else {
					params.append(keylist.get(i)).append(", ");
				}
			}

		}
		return params.toString();
	}

	private String createQuestion(int qtd) {
		final StringBuilder q = new StringBuilder();
		for (int i = 1; i <= qtd; i++) {
			if (i == qtd) {
				q.append("?");
			} else {
				q.append("?, ");
			}
		}
		return q.toString();
	}

	private String tableData(List data) {
		List<Tuple> resp = data;
		JsonArray jsonArray = new JsonArray();
		for (Tuple t : resp) {
			JsonObject jsonObj = new JsonObject();
			List<TupleElement<?>> cols = t.getElements();
			for (TupleElement col : cols) {
				Object value = t.get(col.getAlias());
				if (value == null) {
					jsonObj.add(col.getAlias(), JsonNull.INSTANCE);
				} else if (value instanceof Number) {
					jsonObj.addProperty(col.getAlias(), (Number) value);
				} else if (value instanceof Date) {
					jsonObj.addProperty(col.getAlias(), dateFormat.format(value));
				}else {
					jsonObj.addProperty(col.getAlias(), value.toString());
				}
			}
			jsonArray.add(jsonObj);
		}
		return jsonArray.getAsJsonArray().toString();
	}

	private String[] converterData(String tabela, Map<String, String> objeto) {
		List<Tuple> tuple = getTuple(tabela);
		Set<String> camposData = new HashSet<String>();
		for (Tuple t : tuple) {
			List<TupleElement<?>> cols = t.getElements();
			for (TupleElement col : cols) {
				String alias = col.getAlias();
				String type = col.getJavaType().getName();
				if (type.toLowerCase().contains("time") || type.toLowerCase().contains("date")) {
					camposData.add(alias);
				}
			}
		}
		String[] typeArray = new String[camposData.size()];
		camposData.toArray(typeArray);
		return typeArray;
	}
	public List getTuple(String tabela) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(tabela);
		final Query query = entityManager.createNativeQuery(sql.toString(), Tuple.class);
		return query.getResultList();
	}
	public Map<String, HashSet<String>> doWork(){
		Session session = entityManager.unwrap(Session.class); 
		return session.doReturningWork(new MetadadosWork());
	}
}