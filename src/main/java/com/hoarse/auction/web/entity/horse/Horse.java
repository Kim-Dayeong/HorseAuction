package com.hoarse.auction.web.entity.horse;

import com.hoarse.auction.web.dto.horse.HorseupdateDto;
import com.hoarse.auction.web.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "Horse")
public class Horse {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="HorseId")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String birth;

    @Column(nullable = false)
    private String furcolor;

    private Long bidPrice;

    @Column(unique = true, nullable = false)
    private String uniqueNum; // 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Horse father;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Horse mother;

    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member producer;

    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member owner;

    @OneToMany(mappedBy = "mother", fetch = FetchType.LAZY)
    private List<Horse> motherChildren = new ArrayList<>();

    @OneToMany(mappedBy = "father", fetch = FetchType.LAZY)
    private List<Horse> fatherChildren = new ArrayList<>();

    @Builder
    private Horse(String name, String birth, String furcolor, Horse mother, Horse father
    , Member owner, Member producer, String uniqueNum, Long bidPrice){
        this.name = name;
        this.birth = birth;
        this.furcolor = furcolor;
        this.mother = mother;
        this.father = father;
        this.owner = owner;
        this.producer = producer;
        this.uniqueNum = uniqueNum;
        this.bidPrice = bidPrice;

    }
    public void update(HorseupdateDto updateDto) {
        this.name = updateDto.getName();
        this.birth = updateDto.getBirth();
        this.furcolor = updateDto.getFurcolor();
        this.mother = updateDto.getMother();
        this.father = updateDto.getFather();
        this.owner = updateDto.getOwner();
        this.producer = updateDto.getProducer();
        this.uniqueNum = updateDto.getUniqueNum();
        this.bidPrice = updateDto.getBidPrice();
    }



}
