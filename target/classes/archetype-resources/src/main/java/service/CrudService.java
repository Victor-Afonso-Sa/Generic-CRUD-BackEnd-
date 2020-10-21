package ${package}.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ${package}.metadados.MetadadosUtil;

import ${package}.repository.GenericRepository;

@RestController
@RequestMapping("/${artifactId}")
public class CrudService<T> {

	public String tabela;
	
	@Autowired
	GenericRepository createQuery;
	
	@Autowired
	MetadadosUtil metadados ;
	
		
	@GetMapping("/{tabela}")
	public String getTable(@PathVariable(value = "tabela") String tabela, @RequestParam Map<String, String> query) {
		if (query.size() > 0) {
			metadados.verificacao(tabela, query);
			return createQuery.getByParameter(tabela, query);
		} else {
			return createQuery.getAll(tabela);
		}
	}

	@PostMapping("/{tabela}")
	public String adicionar(@PathVariable(value = "tabela") String tabela,
			@RequestBody List<Map<String, String>> objeto) throws ParseException {
		int response;
		if (objeto.size() > 0) {
			response = createQuery.insert(tabela, objeto);

		} else {
			return objErro();
		}
		return response > 0 ? "Cadastrado com sucesso" : "ERRO";
	}

	@DeleteMapping("/{tabela}")
	public String excluir(@PathVariable(value = "tabela") String tabela, @RequestParam Map<String, String> parametros) {
		int response;
		if (parametros.size() > 0) {
			metadados.verificacao(tabela, parametros);
			response = createQuery.delete(tabela, parametros);
		} else {
			return paramErro();
		}
		return response > 0 ? "Deletado com sucesso" : "ERRO";
	}

	@PutMapping("/{tabela}")
	public String atualizar(@PathVariable(value = "tabela") String tabela, @RequestParam Map<String, String> parametros,
			@RequestBody Map<String, String> objeto) throws ParseException {
		metadados.verificacao(tabela, parametros);
		if (parametros.size() <= 0) {
			return paramErro();
		}
		if (objeto.size() <= 0) {
			return objErro();
		}
		int response = createQuery.update(tabela, parametros, objeto);
		return response > 0 ? "Atualizado com sucesso" : "ERRO";
	}

	public String paramErro() {
		return "Erro essa requisição precisa de paremetros";
	}

	public String objErro() {
		return "Erro essa requisição precisa de um JSON valido";
	}
}
