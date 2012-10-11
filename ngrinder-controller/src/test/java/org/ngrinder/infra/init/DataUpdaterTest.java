package org.ngrinder.infra.init;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.Test;
import org.ngrinder.infra.config.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@SuppressWarnings("deprecation")
public class DataUpdaterTest extends org.ngrinder.AbstractNGrinderTransactionalTest {
	@Autowired
	private LocalContainerEntityManagerFactoryBean entityManagerFactory;

	@Test
	public void exportDatabaseSchema() throws IOException {
		PersistenceUnitInfo persistenceUnitInfo = entityManagerFactory.getPersistenceUnitInfo();
		Map<?, ?> jpaPropertyMap = entityManagerFactory.getJpaPropertyMap();
		Configuration configuration = new Ejb3Configuration().configure(persistenceUnitInfo, jpaPropertyMap).getHibernateConfiguration();
		for (Database each : Database.values()) {
			String string = each.getDialect();
			if(each == Database.cubrid){
				configuration.setProperty(Environment.DIALECT,"org.hibernate.dialect.CUBRIDDialectex");
			}else{
				configuration.setProperty(Environment.DIALECT, string);
			}
			SchemaExport schema = new SchemaExport(configuration);
			File parentFile = new ClassPathResource("/ngrinder_datachange_logfile/db.changelog.xml").getFile().getParentFile();


			parentFile.mkdirs();
			File file = new File(parentFile, "schema-" + each.name().toLowerCase() + ".sql");
			System.out.println(file.getAbsolutePath());
			schema.setOutputFile(file.getAbsolutePath());
			schema.create(true, false);
		}
	}
}