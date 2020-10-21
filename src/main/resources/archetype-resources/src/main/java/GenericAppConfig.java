package ${package}; 
import org.springframework.context.annotation.Configuration;

import ${package}.metadados.MetadadosUtil;

@Configuration
public class GenericAppConfig {

	public MetadadosUtil getMetadados() {
		return new MetadadosUtil();
	}

}
