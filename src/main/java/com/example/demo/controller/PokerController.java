package com.example.demo.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.example.demo.controller.base.SessionCheck;
import com.example.demo.domain.model.Card;
import com.example.demo.domain.model.PokerPlayingInfo;
import com.example.demo.dto.PokerPlayingInfoDto;
import com.example.demo.exception.IllegalBetException;
import com.example.demo.exception.LoginSessionTimeOutException;
import com.example.demo.service.PokerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class PokerController extends SessionCheck {

	@Autowired
	public PokerService pokerService;

	@Autowired
 private DiscoveryClient dc;

///**
// * 所持金情報を返す
// * @return
// * @throws LoginSessionTimeOutException セッションタイムアウトエラー
// */
// @GetMapping("/bet")
//	@ResponseBody
//	public Money getMoney() throws LoginSessionTimeOutException {
//		sessionCheck();
//
//  List<ServiceInstance> serviceList = dc.getInstances("userAp");
//  ServiceInstance serviceInstance = serviceList.get(0);
//  String url = "http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort();// 要修正 TODO
//  ResponseEntity<Money> userApResponse = new RestTemplate().getForEntity(url, Money.class);
//		return userApResponse.getBody();
//	}

/**
 * ベット額が所持金を超えていないかチェックして、ポーカーの初期情報(山札・プレイヤーとCPUの手札)を返す。
 * @param betMoney ベット額
 * @param jokerIncluded ジョーカーを含んでいるかどうか
 * @return
 * @throws LoginSessionTimeOutException セッションタイムアウトエラー
 * @throws IllegalBetException ベット額が所持金を超えているエラー
 */
@PostMapping("/config")
	@ResponseBody
	public PokerPlayingInfoDto postPokerStart(int userId, BigDecimal betMoney, boolean jokerIncluded) throws LoginSessionTimeOutException, IllegalBetException {

		RestTemplate restTemplate = new RestTemplate();
		List<ServiceInstance> pokerWebApServiceList = dc.getInstances("Poker-WebAp");
  ServiceInstance pokerWebInstance = pokerWebApServiceList.get(0);
		String getAccessTokenUrl = "http://" + pokerWebInstance.getHost() + ":" + pokerWebInstance.getPort() + "/access-token";
		ResponseEntity<String> response = restTemplate.getForEntity(getAccessTokenUrl, String.class);
		String accessToken = response.getBody();

		 return PokerPlayingInfoDto.convertPokerPlayingInfoDto(pokerService.pokerPrepare(userId, betMoney, jokerIncluded));
	}

/**
 * 手札交換・役判定を勝者判定を実施
 * @param jsonPlayerHands プレイヤーの手札
 * @param jsonDeck 山札
 * @param jsonComputerHands  CPUの手札
 * @return
 * @throws IOException
 * @throws LoginSessionTimeOutException セッションタイムアウトエラー
 */
@PostMapping("/play")
	@ResponseBody
	public PokerPlayingInfoDto handChange(String jsonPlayerHands, String jsonDeck, String jsonComputerHands) throws IOException, LoginSessionTimeOutException {

		ObjectMapper o = new ObjectMapper();

		List<Card> playerHands = o.readValue(jsonPlayerHands, new TypeReference<List<Card>>(){});
		List<Card> deck = o.readValue(jsonDeck, new TypeReference<List<Card>>(){});
		List<Card> computerHands = o.readValue(jsonComputerHands, new TypeReference<List<Card>>(){});

		return PokerPlayingInfoDto.convertPokerPlayingInfoDto(pokerService.handChangeAfterProcess(PokerPlayingInfo.builder()
				.deck(deck)
				.playerHands(playerHands)
				.computerHands(computerHands)
				.build()));

	}

///**
// * 勝敗に応じて所持金を更新する
// * @param betMoney ベット額
// * @param winner 勝者
// * @return
// * @throws LoginSessionTimeOutException セッションタイムアウトエラー
// */
// @PostMapping("/result")
//	@ResponseBody
//	public MoneyDto result(BigDecimal betMoney, Winner winner) throws LoginSessionTimeOutException {
//
//	  sessionCheck();
//
//		 List<ServiceInstance> serviceList = dc.getInstances("userAp");
//	  ServiceInstance serviceInstance = serviceList.get(0);
//	  String url = "http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort();// 要修正 TODO
//	  ResponseEntity<Money> userApResponse = new RestTemplate().getForEntity(url, Money.class);
//			return userApResponse.getBody();
//
//	}

}
