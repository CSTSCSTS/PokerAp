package com.example.demo.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.demo.dto.MoneyDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Money {

	public int userId;
	public BigDecimal money;
	public LocalDateTime updateDate;


	public void plusMoney(BigDecimal betMoney) {
   money = money.add(betMoney);
	}

 public void minusMoney(BigDecimal betMoney) {
 	 money = money.subtract(betMoney);
	}

 public static Money convertMoney(MoneyDto moneyDto) {
			return new Money(
							moneyDto.getUserId(),
							new BigDecimal(moneyDto.getMoney()),
							LocalDateTime.parse(moneyDto.getUpdateDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
	}

}
