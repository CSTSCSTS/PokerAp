package com.example.demo.dto;

import java.time.format.DateTimeFormatter;

import com.example.demo.domain.model.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyDto {

	public int userId;
	public String money;
	public String updateDate;

	public static MoneyDto convertMoneyDto(Money money) {
			return MoneyDto.builder()
  						.userId(money.getUserId())
  						.money(money.getMoney().toString())
  						.updateDate(money.getUpdateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")))
  						.build();

	}

}
