package com.arrenaid.entity;

import com.arrenaid.controller.State;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "bot_state")
    private State state;
    @Column(name = "chat_id",unique = true)
    @NotNull
    private int chatId;
    @Column
    private int score;
    @Column
    private String username;

    public User(State state, int chatId) {
        this.state = state;
        this.chatId = chatId;
    }
}
