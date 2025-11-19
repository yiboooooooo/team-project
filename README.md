# Team Project

[Please keep this up-to-date with information about your project throughout the term.

The readme should include information such as:

- a summary of what your application is all about
- a list of the user stories, along with who is responsible for each one
- information about the API(s) that your project uses
- screenshots or animations demonstrating current functionality

By keeping this README up-to-date,
your team will find it easier to prepare for the final presentation
at the end of the term.]

**Application Summary:**<br>
The application, StakeMate, is a sports betting application which will be implemented as an educational, play-money
sports-betting exchange. It will be an application where users place and match orders on game outcomes using a
daily-updated data feed (24-hour API refresh) and automatic post-game settlement.

**User Stories:**<br>
User Story 1 (implemeneted by Firas Adnan Jalil): As a sports bettor, I wish to see continuously updated data, so that I
can stay informed about the market.

User Story 2 (implemented by Gary Choi): As a sports bettor, I want to browse active matches, see each market's live
order book, and use clear buy/sell controls, so that I can place (fake-money) bets quickly and know when a market
closes.

User Story 3 (implemented by Yibo Sun): As a sports bettor, I want my account, bankroll, and bets to be safely stored in
the database and for screens to update automatically when data changes, so that I always see accurate balances and
market status without refreshing.

User Story 4 (implemented by Tarunraj Kalyanasundaram): As a sports bettor, I want to place limit and market orders on
any open market and have them matched fairly and immediately, so that I can buy/sell at my chosen price or at the best
available price.

User Story 5 (implemented by Erzheng Zhang): As a sports bettor, I wish to leave public comments regarding the market,
so that I can interact with other users about sports betting.

User Story 6 (implemented by Abdulghafur Alshanqiti): As a sports bettor, I would like to find out if I won or lost, so
that I can see how much money I won or lost.

**API(s):**<br>
API Name: Odds API (https://the-odds-api.com)<br><br>
Main Service Provided:<br>

- List of in-season sports (optionally all sports) (doesn’t count against quota)<br>
- Upcoming and live games with recent bookmaker odds (by sport, region, and market)<br>
- Upcoming, live, and recent games; live scores update ~every 30s<br>
- In-play and pre-match event list (IDs, teams, start time), no odds (free/no quota)<br>
- Odds for a single event; accepts many market keys<br>
- Available market keys per bookmaker for that event<br>
- Teams/players for a sport (participant list)<br>

Status: Successfully tested with OkHttp in Java

**Screenshots or Animations of Current Functionality:**

**Group TUT0401-24 Members:**<br>
Gary Choi, Yibo Sun, Erzheng Zhang, Abdulghafur Alshanqiti, Firas Adnan Jalil, and Tarunraj Kalyanasundaram
