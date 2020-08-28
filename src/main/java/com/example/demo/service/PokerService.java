package com.example.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.domain.model.Card;
import com.example.demo.domain.model.Money;
import com.example.demo.domain.model.PokerPlayingInfo;
import com.example.demo.domain.model.PokerPlayingInfo.Winner;
import com.example.demo.domain.model.Role;
import com.example.demo.domain.model.checker.Checker;
import com.example.demo.dto.MoneyDto;
import com.example.demo.exception.IllegalBetException;
import com.example.demo.util.PokerUtil;

@Service
public class PokerService {

	 @Autowired
		protected MessageSource messageSource;

		@Autowired
	 private DiscoveryClient dc;


// ベット額が所持金を超えていないことを確認後、ポーカーの初期情報を返す
	public PokerPlayingInfo pokerPrepare(int userId, BigDecimal betMoney, boolean jokerIncluded) throws IllegalBetException {

		RestTemplate restTemplate = new RestTemplate();
		List<ServiceInstance> userApServiceList = dc.getInstances("UserAp");
  ServiceInstance userApInstance = userApServiceList.get(0);
		String getMoneyUrl = "http://" + userApInstance.getHost() + ":" + userApInstance.getPort() + "/money?userId={userId}";
		// 所持金を取得
  Money money = Money.convertMoney(restTemplate.getForEntity(getMoneyUrl, MoneyDto.class, userId).getBody());
		// ベット額が所持金を超えている場合、例外を投げる
		if(betMoney.compareTo(money.getMoney()) > 0) {
		  throw new IllegalBetException(messageSource.getMessage("illegal.bet", null, Locale.JAPAN));
		}

		// ポーカーの山札・プレイヤー・CPUの手札を返す
		return pokerSetUp(jokerIncluded);
	}

	public PokerPlayingInfo pokerSetUp(boolean jokerIncluded) {

		List<Card> deck = Card.makeDeck(jokerIncluded);

		Collections.shuffle(deck);
		List<Card> playerHands = new ArrayList<>();
		for(int i=0; i<5; i++) {
			playerHands.add(deck.remove(i));
		}
		List<Card> cpuHands = new ArrayList<>();
		for(int i=0; i<5; i++) {
			cpuHands.add(deck.remove(i));
		}
		return PokerPlayingInfo.builder()
				.deck(deck)
				.playerHands(playerHands)
				.computerHands(cpuHands)
				.build();


	}

	// ポーカーの手札交換・役判定・勝者判定を実施する
	public PokerPlayingInfo handChangeAfterProcess(PokerPlayingInfo info) {

		// プレイヤーの手札交換をした後、役判定
		List<Card> afterPlayerHandChange = handChange(info.getPlayerHands(), info.getDeck());
		Checker checker = PokerUtil.makeChecker();
		info.setPlayerHands(afterPlayerHandChange);
		info.setPlayerRole(roleCheck(afterPlayerHandChange, checker));


		// 初期状態の役から、何を交換するか判断する。
		Role currentCpuRole = roleCheck(info.getComputerHands(), checker);

		List<Card> afterCpuHandChange = handChange(exchangeTargetDecision(currentCpuRole, info.getComputerHands()), info.getDeck());

		// CPUの手札交換をした後、役判定
		info.setComputerHands(afterCpuHandChange);
		info.setComputerRole(roleCheck(afterCpuHandChange, checker));
		info.setFinishedChange(true);
		info.setWinner(WinOrLossJudge(info.getPlayerRole().get(), info.getComputerRole().get()));

		return info;

	}

	public Role roleCheck (List<Card> hands, Checker checker) {
    return checker.check(hands).orElseThrow(() -> new RuntimeException(messageSource.getMessage("no.role", null, Locale.JAPAN)));
	}

	public List<Card> exchangeTargetDecision(Role currentCpuRole, List<Card> cpucards) {

		if(isNoChange(currentCpuRole)) {
			return cpucards;
		}

		switch(currentCpuRole.getRoleName()) {
		  case FOUR_CARD:
		  	fourCardExchangeTargetDecision(cpucards);
		  	break;
		  case THREE_CARD:
		  	threeCardExchangeTargetDecision(cpucards);
		  	break;
		  case TWO_PAIR:
		  	twoPairExchangeTargetDecision(cpucards);
		  	break;
		  case ONE_PAIR:
		  	onePairExchangeTargetDecision(cpucards);
		  	break;
		  case HIGH_CARD:
		  	highCardExchangeTargetDecision(cpucards);
		  	break;
			case FIVE_CARD:
				break;
			case FLUSH:
				break;
			case FULL_HOUSE:
				break;
			case ROYEL_STRAIGHT_FLUSH:
				break;
			case STRAIGHT:
				break;
			case STRAIGHT_FLUSH:
				break;
			default:
				break;
		}

		return cpucards;
	}

	// 4カードを構成しているカードでないもののisChangedをtrueにする。
	private List<Card> fourCardExchangeTargetDecision(List<Card> cpucards) {
		List<Card> noDuplicateList = PokerUtil.noDupilicateNumberCards(cpucards);
		for(int i=0;i<noDuplicateList.size();i++) {
			Card card = noDuplicateList.get(0);
			cpucards.stream()
			.filter(c -> c.equals(card))
			.forEach(c -> c.setIsChange(true));
		}
		return cpucards;
	}

//3カードを構成しているカードでないもののisChangedをtrueにする(trueにする枚数は1 or 2)。
	private List<Card> threeCardExchangeTargetDecision(List<Card> cpucards) {
		List<Card> noDuplicateList = PokerUtil.noDupilicateNumberCards(cpucards);
		for(int i=0; i < changeCardNumber(1, 2); i++) {
			int number = i;
			cpucards.stream()
			.filter(c -> c.equals(noDuplicateList.get(number)))
			.forEach(c -> c.setIsChange(true));
		}

		return cpucards;
	}

//2ペアを構成しているカードでないもののisChangedをtrueにする()。
	private List<Card> twoPairExchangeTargetDecision(List<Card> cpucards) {
		List<Card> noDuplicateList = PokerUtil.noDupilicateNumberCards(cpucards);
		for(int i=0;i<noDuplicateList.size();i++) {
			Card card = noDuplicateList.get(0);
			cpucards.stream()
			.filter(c -> c.equals(card))
			.forEach(c -> c.setIsChange(true));
		}
		return cpucards;
	}

 //1ペアを構成しているカードでないもののisChangedをtrueにする(trueにする枚数は2 or 3)。
	private List<Card> onePairExchangeTargetDecision(List<Card> cpucards) {
		List<Card> noDuplicateList = PokerUtil.noDupilicateNumberCards(cpucards);
		for(int i=0; i < changeCardNumber(2, 3); i++) {
			int number = i;
			cpucards.stream()
			.filter(c -> c.equals(noDuplicateList.get(number)))
			.forEach(c -> c.setIsChange(true));
		}
		return cpucards;
	}

	//highカードを構成しているカードでないもののisChangedをtrueにする(trueにする枚数は1 or 4 or 5)。
	private List<Card> highCardExchangeTargetDecision(List<Card> cpucards) {

		// フラッシュを狙える手札かどうか判定する→狙えそうなら1枚すてる。
		if(PokerUtil.nearFlush(cpucards)) {
			return PokerUtil.noDupilicateMarkCardChange(cpucards);
		}

		// ストレートを狙える手札かどうか判定する
		if(PokerUtil.nearStraight(cpucards)) {
			return PokerUtil.changeFlagDisturbedCardToMakeStraight(cpucards);
		}

		for(int i=0; i < changeCardNumber(4, 5); i++) {
			cpucards.get(i).setIsChange(true);
		}
		return cpucards;
	}

	private boolean isNoChange(Role currentCpuRole) {
		return Role.noChangeRoles().stream()
		.filter(r -> r.equals(currentCpuRole))
		.findFirst().isPresent();
	}

	public Winner WinOrLossJudge(Role playerRole, Role computerRole) {
		int playerRolePower = playerRole.getRoleNumber();
		int computerRolePower = computerRole.getRoleNumber();

		if(playerRolePower > computerRolePower) {
      return Winner.PLAYER;
		} else if(playerRolePower < computerRolePower) {
			return Winner.CPU;
		}
		return Winner.NOTHING;
	}

	public List<Card> handChange(List<Card> cards, List<Card> deck) {
	  // 交換する手札をリストから削除
		List<Card> keepHands = cards.stream()
				.filter(h -> h.getIsChange() == false)
				.collect(Collectors.toList());

		int keepHandsCount = keepHands.size();

				// 削除した要素分引く。
				for(int i=0; i<5 - keepHandsCount; i++) {
					keepHands.add(deck.remove(i));
				}
				return keepHands;
	}

	private static int changeCardNumber(int number1, int number2) {
		Random random = new Random();
		if(random.nextInt(100) >= 50) {
      return number1;
		}
		return number2;
	}

}
