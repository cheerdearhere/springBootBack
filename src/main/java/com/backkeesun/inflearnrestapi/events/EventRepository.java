package com.backkeesun.inflearnrestapi.events;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event,Integer> { //JPA로 만드는 repository
}
