package org.ngrinder.agent.repository;

import org.ngrinder.agent.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
	Connection findByIpAndPort(String ip, int port);
}
