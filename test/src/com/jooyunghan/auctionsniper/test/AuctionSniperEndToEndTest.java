package com.jooyunghan.auctionsniper.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.jooyunghan.auctionsniper.ui.MainActivity;

public class AuctionSniperEndToEndTest extends
		ActivityInstrumentationTestCase2<MainActivity> {
	private FakeAuctionServer auction = new FakeAuctionServer("item-54321");
	private FakeAuctionServer auction2 = new FakeAuctionServer("item-65432");
	private ApplicationRunner application = new ApplicationRunner();
	private Solo solo;

	public AuctionSniperEndToEndTest() {
		super(MainActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testSniperJoinsAuctionUntilAuctionCloses() throws Exception {
		auction.startSellingItem();
		application.startBiddingIn(solo, auction);
		auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);
		auction.announceClosed();
		application.showsSniperHasLostAuction(auction);
	}

	public void testSniperMakesAHigherBidButLoses() throws Exception {
		auction.startSellingItem();

		application.startBiddingIn(solo, auction);
		auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

		auction.reportPrice(1000, 98, "other bidder");
		application.hasShownSniperIsBidding(auction, 1000, 1098);

		auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

		auction.announceClosed();
		application.showsSniperHasLostAuction(auction);
	}

	public void testSniperWinsAnAuctionByBiddingHigher() throws Exception {
		auction.startSellingItem();

		application.startBiddingIn(solo, auction);
		auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

		auction.reportPrice(1000, 98, "other bidder");
		application.hasShownSniperIsBidding(auction, 1000, 1098);

		auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

		auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
		application.hasShownSniperIsWinning(auction, 1098);

		auction.announceClosed();
		application.showsSniperHasWonAuction(auction, 1098);
	}

	public void testSniperBidsForMultipleItems() throws Exception {
		auction.startSellingItem();
		auction2.startSellingItem();

		application.startBiddingIn(solo, auction, auction2);
		auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);
		auction2.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

		auction.reportPrice(1000, 98, "other bidder");
		auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

		auction2.reportPrice(500, 21, "other bidder");
		auction2.hasReceivedBid(521, ApplicationRunner.SNIPER_XMPP_ID);

		auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
		auction2.reportPrice(521, 22, ApplicationRunner.SNIPER_XMPP_ID);

		application.hasShownSniperIsWinning(auction, 1098);
		application.hasShownSniperIsWinning(auction2, 521);

		auction.announceClosed();
		auction2.announceClosed();

		application.showsSniperHasWonAuction(auction, 1098);
		application.showsSniperHasWonAuction(auction2, 521);
	}

	public void testSniperLosesWhenThePriceIsTooHigh() throws Exception {
		auction.startSellingItem();
		application.startBiddingWithStopPrice(solo, auction, 1100);
		auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

		auction.reportPrice(1000, 98, "Other bidder");
		application.hasShownSniperIsBidding(auction, 1000, 1098);
		auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

		auction.reportPrice(1197, 10, "Second party");
		application.hasShownSniperIsLosing(auction, 1197, 1098);

		auction.reportPrice(1298, 10, "Third party");
		application.hasShownSniperIsLosing(auction, 1298, 1098);

		auction.announceClosed();
		application.showsSniperHasLostAuction(auction);
	}

	public void testSniperReportsInvalidAuctionMessageAndStopsRespondingToEvents()
			throws Exception {
		final String brokenMessage = "a broken message";
		auction.startSellingItem();
		auction2.startSellingItem();

		application.startBiddingIn(solo, auction, auction2);
		auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

		auction.reportPrice(500, 20, "other bidder");
		auction.hasReceivedBid(520, ApplicationRunner.SNIPER_XMPP_ID);

		auction.sendInvalidMessageContaining(brokenMessage);
		application.showsSniperHasFailed(auction);

		auction.reportPrice(520, 21, "other bidder");
		waitForAnotherAuctionEvent();

		application.reportsInvalidMessage(auction, brokenMessage);
		application.showsSniperHasFailed(auction);
	}

	private void waitForAnotherAuctionEvent() throws Exception {
		auction2.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);
		auction2.reportPrice(600, 6, "other bidder");
		application.hasShownSniperIsBidding(auction2, 600, 606);
	}

	@Override
	public void tearDown() throws Exception {
		auction.stop();
		auction2.stop();
		application.stop();
		super.tearDown();
	}
}
