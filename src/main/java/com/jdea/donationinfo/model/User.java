package com.jdea.donationinfo.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProviders;

import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="USER")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ID;
	
	private String name;
	private String username;
	private String password;
    private String session_id;
    private Timestamp login_daate;
}
