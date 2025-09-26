package com.eventwise.event.domain;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
private String title;
private String category;
private int capacity;
}