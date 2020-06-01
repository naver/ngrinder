package org.ngrinder.agent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.ngrinder.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@ToString
@Entity
@Table(name = "CONNECTION")
public class Connection extends BaseEntity<Connection> {
	private String ip;
	private String name;
	private int port;
	private String region;
}
