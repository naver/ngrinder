package org.ngrinder.agent.model;

import lombok.*;
import org.ngrinder.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@ToString
@Entity
@Table(name = "CONNECTION")
@NoArgsConstructor
@AllArgsConstructor
public class Connection extends BaseEntity<Connection> {
	private String ip;
	private String name;
	private int port;
	private String region;
}
