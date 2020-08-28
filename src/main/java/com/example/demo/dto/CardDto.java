package com.example.demo.dto;

import com.example.demo.domain.model.Card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDto {

	public String type;
	public int number;
	public boolean isChange;

	public static CardDto convertRoleDto(Card card) {
			return CardDto.builder()
							.type(card.getType().toString())
							.number(card.getNumber())
							.isChange(card.getIsChange())
							.build();
	}

}
