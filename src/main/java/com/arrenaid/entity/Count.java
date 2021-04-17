package com.arrenaid.entity;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "count")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Count {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "chat_id")
    @NotNull
    private int chatId;
    @Column
    @NonNull
    private int score;
    @Column(name = "score_name")
    @NonNull
    private String name;
    @Column(name = "number_changes")
    private int num;

    public Count(int chatId, @NonNull int score, @NonNull String name) {
        this.chatId = chatId;
        this.score = score;
        this.name = name;
        this.num = 1;
    }
}
