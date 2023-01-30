package com.jdea.donationinfo.repository;

// import java.util.List;
import com.jdea.donationinfo.model.DonationInfo;

import java.sql.Date;
import java.util.List;

// import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.repository.CrudRepository;
// import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DonationInfoRepository extends JpaRepository<DonationInfo, Long> {
    // List<DonationInfo> findByName(String name);
    // DonationInfo findById(long id);
    // List<DonationInfo> findAllByPrice(price, Pageable pageable);
    Page<DonationInfo> findByNameContaining(String name, Pageable pageable);
    Page<DonationInfo> findByDonatedate(Date donate_date, Pageable pageable);
    Page<DonationInfo> findByNameContainingAndDonatedate(String name, Date donate_date, Pageable pageable);

}

// @Repository
// public interface DonationInfoRepository extends PagingAndSortingRepository<DonationInfo, Long> {
//     // List<DonationInfo> findByName(String name);
//     // DonationInfo findById(long id);
//     // List<DonationInfo> findAllByPrice(price, Pageable pageable);
//     Page<DonationInfo> findByName(String name, PageRequest pageable);
//     Page<DonationInfo> findByDonatedate(Date donate_date, PageRequest pageable);
//     Page<DonationInfo> findByNameAndDonatedate(String name, Date donate_date, PageRequest pageable);

// }