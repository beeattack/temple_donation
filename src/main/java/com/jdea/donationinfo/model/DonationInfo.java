package com.jdea.donationinfo.model;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
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
@Table(name="DONATIONINFO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationInfo {
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
	private Long ID;

	@Column(name = "NAME", length = 400)
	private String name;

	@Column(name = "DONATE_FOR", length = 400)
	private String DONATE_FOR;

	@Column(name = "AMOUNT")
  private Double AMOUNT;

  private String AMOUNT_TH;

	@Column(name = "DONATE_DATE")
  private Date donatedate;

	private String donateDateTh;

	@Column(name = "RECORD_DATE")
   private Timestamp RECORD_DATE;

	
}
