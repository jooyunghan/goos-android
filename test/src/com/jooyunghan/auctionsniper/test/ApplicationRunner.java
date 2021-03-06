package com.jooyunghan.auctionsniper.test;

import static org.hamcrest.Matchers.containsString;

import java.io.IOException;

import com.jayway.android.robotium.solo.Solo;
import com.jooyunghan.auctionsniper.SniperState;
import com.jooyunghan.auctionsniper.ui.MainActivity;
import com.jooyunghan.auctionsniper.ui.SnipersAdapter;

public class ApplicationRunner {
	public static final String XMPP_HOSTNAME = "localhost";
	public static final String SNIPER_XMPP_ID = "sniper";
	public static final String SNIPER_XMPP_PASSWORD = "sniper";
	private AuctionSniperDriver driver;
	private AuctionLogDriver logDriver = new AuctionLogDriver();
	private MainActivity activity;

	public void startBiddingWithStopPrice(Solo solo, FakeAuctionServer auction,
			int stopPrice) throws InterruptedException {
		startSniper(solo);
		final String itemId = auction.getItemId();
		driver.startBiddingFor(itemId, stopPrice);
		driver.showsSniperState(itemId, 0, 0, textFor(SniperState.JOINING));
	}

	public void startBiddingIn(Solo solo, final FakeAuctionServer... auctions)
			throws Exception {
		startSniper(solo);
		for (FakeAuctionServer auction : auctions) {
			final String itemId = auction.getItemId();
			driver.startBiddingFor(itemId);
			driver.showsSniperState(itemId, 0, 0, textFor(SniperState.JOINING));
		}
	}

	private void startSniper(Solo solo) {
		driver = new AuctionSniperDriver(solo, 5000);
		activity = (MainActivity) solo.getCurrentActivity();
		logDriver.clearLog();
	}

	private String textFor(SniperState state) {
		return SnipersAdapter.textFor(activity, state);
	}

	public void showsSniperHasLostAuction(FakeAuctionServer auction)
			throws InterruptedException {
		driver.showsSniperState(auction.getItemId(), textFor(SniperState.LOST));
	}

	public void showsSniperHasWonAuction(FakeAuctionServer auction,
			int lastPrice) throws InterruptedException {
		driver.showsSniperState(auction.getItemId(), lastPrice, lastPrice,
				textFor(SniperState.WON));
	}

	public void showsSniperHasFailed(FakeAuctionServer auction)
			throws InterruptedException {
		driver.showsSniperState(auction.getItemId(), 0, 0,
				textFor(SniperState.FAILED));
	}

	public void hasShownSniperIsBidding(FakeAuctionServer auction,
			int lastPrice, int lastBid) throws InterruptedException {
		driver.showsSniperState(auction.getItemId(), lastPrice, lastBid,
				textFor(SniperState.BIDDING));
	}

	public void hasShownSniperIsWinning(FakeAuctionServer auction, int lastPrice)
			throws InterruptedException {
		driver.showsSniperState(auction.getItemId(), lastPrice, lastPrice,
				textFor(SniperState.WINNING));
	}

	public void hasShownSniperIsLosing(FakeAuctionServer auction,
			int lastPrice, int lastBid) throws InterruptedException {
		driver.showsSniperState(auction.getItemId(), lastPrice, lastBid,
				textFor(SniperState.LOSING));
	}

	public void reportsInvalidMessage(FakeAuctionServer auction, String message)
			throws IOException {
		logDriver.hasEntry(containsString(message));
	}

	public void stop() {
		if (driver != null) {
			driver.dispose();
		}
	}

}
