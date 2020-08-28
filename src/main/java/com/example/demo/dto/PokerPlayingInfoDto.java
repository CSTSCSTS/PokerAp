package com.example.demo.dto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.demo.domain.model.PokerPlayingInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PokerPlayingInfoDto {

	public List<CardDto> deck;
	public List<CardDto> playerHands;
	public List<CardDto> computerHands;
	public RoleDto playerRole;
	public RoleDto computerRole;
	private String winner;
	private boolean isFinishedChange;

	public static PokerPlayingInfoDto convertPokerPlayingInfoDto(PokerPlayingInfo info) {

			return PokerPlayingInfoDto.builder()
							.deck(info.getDeck().stream()
											.map(d -> CardDto.convertRoleDto(d))
											.collect(Collectors.toList()))
							.playerHands(info.getPlayerHands().stream()
											.map(p -> CardDto.convertRoleDto(p))
											.collect(Collectors.toList()))
							.computerHands(info.getComputerHands().stream()
											.map(c -> CardDto.convertRoleDto(c))
											.collect(Collectors.toList()))
							.playerRole(info.getPlayerRole().map(p -> RoleDto.convertRoleDto(p)).orElse(null))
							.computerRole(info.getComputerRole().map(c -> RoleDto.convertRoleDto(c)).orElse(null))
							.winner(info.getWinner().map(w -> w.toString()).orElse(null))
							.isFinishedChange(info.isFinishedChange())
							.build();
	}

	public Optional<RoleDto> getPlayerRole() {
			return Optional.ofNullable(playerRole);
	}

	public Optional<RoleDto> getComputerRole() {
			return Optional.ofNullable(computerRole);
	}

	public Optional<String> getWinner() {
			return Optional.ofNullable(winner);
	}

}
