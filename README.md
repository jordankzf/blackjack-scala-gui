![](media\image1.jpg){width="2.526042213473316in"
height="1.0485454943132109in"}

**NET3204: Distributed System**

**Group Assignment**

**Lecturer:** Dr. Chin Teck Min

**Group Name:** Prerequisite

**Members:**

  ----------------------- ----------
  **Name**                **ID**
  Wong Chuen Ting         17035528
  Jordan Kee Zheng Feng   17029752
  Nicholas Ng Yee Jet     17003625
  Gan Wei Qi              17032418
  ----------------------- ----------

**Table of Contents**

[**1. Introduction to Proposed
System**](#introduction-to-proposed-system) **3**

**[2. Physical and Interactive Model](#physical-and-interactive-model)**
**4**

> [System Architecture Design](#system-architecture-design) 4
>
> [Communication Protocol Sequence
> Diagram](#communication-protocol-sequence-diagram) 5
>
> [Communication between Client Actor and Server
> Actor](#communication-between-client-actor-and-server-actor) 5
>
> [Communication between multiple Client actors (in
> Lobby)](#communication-between-multiple-client-actors-in-lobby) 7
>
> [Communication between multiple Client actors (in
> Game)](#communication-between-multiple-client-actors-in-game) 9

**[3. Scalability & Reliability of
System](#scalability-reliability-of-system)** **12**

> [Scalability of the System](#scalability-of-the-system) 12
>
> [Reliability of the System](#reliability-of-the-system) 14

**[4. Test Case Proposed & Result](#test-case-proposed-result)** **17**

> [Test Cases](#test-cases) 17
>
> [Internet Connectivity](#internet-connectivity) 19
>
> [Additional Screenshots](#additional-screenshots) 21

**[5. Personal Reflections](#personal-reflections)** **32**

> [Wong Chuen Ting (17035528)](#wong-chuen-ting-17035528) 32
>
> [Jordan Kee Zheng Feng (17029752)](#jordan-kee-zheng-feng-17029752) 34
>
> [Nicholas Ng Yee Jet (17003625)](#nicholas-ng-yee-jet-17003625) 36
>
> [Gan Wei Qi (17032418)](#gan-wei-qi-17032418) 38

# 1. Introduction to Proposed System

The distributed system that will be proposed in this project is
Blackjack. It will be using the Akka framework as the middleware and the
Model-View-Controller (MVC) architectural pattern to design the system.
Blackjack is a popular multiplayer gambling card game in which the
player/s compete against a dealer to acquire a higher total value of
cards than the dealer without busting. Bust is a term used in Blackjack
to signify when the player or dealer exceeds a sum total of 21 in their
hands, automatically losing the round. A standard Blackjack game rules
are as follows:

i\. Players place their bets

ii\. Players and dealer receive two cards each

iii\. Each player decide to **hit** (receive more cards) or **stand**
(end turn)

iv\. Dealer decides to hit or stand

v\. If the dealer busts, players that did not bust wins.

vi\. If the dealer did not bust, any player with a higher total than the
dealer wins.

The purpose of this Blackjack game is to allow individuals, friends, and
family to have an enjoyable and cozy game of Blackjack anywhere anytime.
In view of the covid-19 pandemic, the Government of Malaysia has imposed
and encouraged the people to adhere to the Standard Operating Procedures
(SOP) and practices to prevent the transmission of the virus. As a
result, large gatherings in Malaysia are prohibited for an indefinite
period of time. With the Lunar New Year festival quickly approaching,
the thoughts and excitement of spending time with family and friends are
also quickly declining. In this case, the proposed system can be a
solution in providing limitless fun and entertainment to people during
the festival season and getting them in the festive mood at the comfort
of their homes safely during this difficult time.

# 2. Physical and Interactive Model

## **System Architecture Design**

![](media\image2.png){width="6.5in" height="1.8888888888888888in"}

The system architecture design of the Blackjack application consists of
Client-Server architecture and Model-View-Controller design for
operation and execution.

In the client-server architecture, the clients are the players that will
be requesting and receiving a limited number of services from the
server. The server on the other hand will be providing services such as
initializing the game lobby, keeping track of the reachable clients, and
creating the game lobby with the available clients. Whenever a client
has successfully established a connection with the server, the server
will store the client's information for matchmaking purposes. This
allows other clients that are also connected to the server to be visible
to each other. Thus, enabling clients to invite other clients to the
same game lobby with a maximum number of 3 clients per game lobby. Both
clients and the server are communicating through message passing between
each actor node.

For scalability purposes, the server is not responsible for any updates
regarding the interface of the clients during the game of Blackjack.
Instead, the clients will be communicating directly with one another.
For instance, when a client increases his bet amount, other clients
within the same game lobby will receive instant changes to their
respective interfaces that signifies the changes made by the client. The
exchange of information between each client is done using the
Model-View-Controller design architecture.

## **Communication Protocol Sequence Diagram**

## Communication between Client Actor and Server Actor

![](media\image3.png){width="5.89413167104112in"
height="7.3147681539807525in"}

Initially, the server actor will join the cluster node. It will then
subscribe to ReachabilityChange events so that the cluster receptionist
can inform the server of ReachabilityChange events such as whether a
node joins or leaves the cluster.

The same goes for the client actor, which will join the cluster, and
subscribe to ReachabilityChange events to receive the events. The start
message will initiate the Client actor to send the FindTheServer message
to the cluster receptionist which is to subscribe to events related to
the server based on the ServerKey which also refers to the server actor.
The cluster receptionist will then reply with a ListingResponse message
with a list of server actors. In this distributed system, there is only
1 server actor, so the client actor will only receive a listing with 1
server actor.

When the client receives a StartJoin message, it will send a JoinChat
message to a server actor. Once the server actor receives the message,
the server actor will add the member's ActorRef to its list of members
and send a Joined message to the client actor with a list of members.
Whenever there are any changes to the list of members such as when a
client joins or leaves the server actor, the server actor will send the
MemberList message with its updated members list to all existing
members. Finally, the client actor can send a Leave message to the
server actor to indicate that it is leaving the lobby.

## Communication between multiple Client actors (in Lobby)

![](media\image4.png){width="6.140625546806649in"
height="7.881189851268592in"}

In this diagram, Client Actor 1 represents the room host, while Client
Actor 2 represents the room member. Initially, Client Actor 1 receives a
NewRoom message to create a new room by making itself the host. Client
Actor 1 can send an IsInvitable message to Client Actor 2, and receive a
reply of IsInvitableResponse to indicate whether Client Actor 2 is
available to be invited or not. Client Actor 2 is available if it is not
in any existing room. Client Actor 1 will then receive a SendInvitation
message from its controller which will then send the ReceiveInvitation
message to Client Actor 2.

Client Actor 2 can choose to accept or reject the invitation. If Client
Actor 2 accepts, it receives the AcceptInvitation message from its
controller, which then sends the InvitationResponse message to Client
Actor 1. If Client Actor 2 rejects, it receives the RejectInvitation
message from its controller, which then sends the InvitationResponse
message to Client Actor 1.

When Client Actor 1 receives an InvitationResponse from a new client
actor who accepted its invitation, it will send the AddToRoomList to all
clients in the existing room to update the list of members currently in
the room. If a client actor leaves, it will send the RemoveFromRoomList
message to all existing members in the room instead.

When Client Actor 1 who is the room host decides to leave the room, it
will receive the HostLeaveRoom message from its controller, which then
sends the HostLeaveRoomReceive message to all members in the room. All
members involved with the room would receive a ResetRoomList from its
controller to reset its room list so that it can join another room. When
Client Actor 2 who is the room member decides to leave the room, it will
receive the ClientLeaveRoom message from its controller, which then
sends the ClientLeaveRoomReceive message to all members in the room.

When it's time to start a game, Client Actor 1 (room host) will receive
GameStart message from its controller, which then sends the
GameStartReceive message to all members in the room to initiate the
loading of the game.

## Communication between multiple Client actors (in Game)

![](media\image5.png){width="4.166235783027122in"
height="8.342732939632546in"}

In this diagram, Client Actor 1 represents the game host while Client
Actor 2 represents the game client. While Client Actor 1 is the host, it
is also one of the players in the game. First, Client Actor 1 will
receive the RoundStart message from its controller when the game is
initialised, which then sends the RoundStartReceive message to all other
clients in the game.

During the betting phase of the game, each client actor can choose to
increase, decrease or confirm its bet. To increase bet, the client actor
will receive an IncreaseBetSend message from its controller, which then
sends the IncreaseBetReceive message to the game host. To decrease bet,
the client actor will receive an DecreaseBetSend message from its
controller, which then sends the DecreaseBetReceive message to the game
host. Whenever the game host receives an IncreaseBetReceive or
DecreaseBetReceive message, it will send an UpdateBet message to all
client actors including itself for the controller to update the bet
amount. Finally, when each client actor decides to confirm its bet, it
will receive the ConfirmBetSend message from its controller, which then
sends the ConfirmBetReceive message to the game host. The client actor
who is the host will send a ConfirmBetReceiveAck message to whoever
Client Actor which send it the ConfirmBetReceive message (which can
include itself), to tell the controller to disable the betting buttons.

When it is time to distribute the cards, the client actor (host) will
receive the GiveCard message from its controller, which then sends to
all client actors including itself the ReceiveCard message for its
controller to update the cards being displayed on each client's screen.

The client actor (host) will decide the turn order internally and
receive the AnnounceTurn message from its controller, which then sends
to all client actors including itself the AnnounceTurnReceive message to
announce which client actor's turn it is in the game.

During a client actor's turn, it can choose to hit or stand. If the
client actor chooses to hit, it will send the PlayerHit message to the
host (who can be itself). The host will reply with a PlayerHitReceive
message to the client actor who sent the PlayerHit message to indicate
that the host has received the message and to tell the controller to
disable the hit/stand buttons. If the client actor chooses to stand, it
will send the PlayerStand message to the host (who can be itself). The
host will reply with a PlayerStandReceive message to the client actor
who sent the PlayerStand message to indicate that the host has received
the message and to tell the controller to disable the hit/stand buttons.

Once all client actor's had their turns, the host client actor will
reveal the dealer's card by sending itself the GiveCard message, which
then sends the ReceiveCard message to all client actors. The host will
calculate the win results for each player. If the dealer wins against
all players, it will receive the AnnounceHouseWin message from its
controller, which then sends the AnnounceHouseWinReceive message to all
client actors to indicate that the house (dealer) won. The host will
customise the win result for each player depending on their cards, and
send itself the AnnounceWinResult message which targets each client
actor, and sends the AnnounceWinResultReceive message to the client
actor. Finally, the host will send the UpdateBalAndBet message to all
client actors the updated balance and bet amount after the winning
calculation. The next round button will be revealed for the host, which
will initiate the next round using the RoundStart message.

The leaving mechanism is the same as in the lobby. If a client actor
(player) left the game, it sends the ClientLeaveRoom message to all
client actors (except itself) and sends the ResetRoomList message to
itself so that it can join another room. Other client actors who are
still in the game will see that the client has disconnected through a
visual indicator. If the host client actor left the game, it will send
the HostLeaveRoom message to all client actors (except itself) and send
the ResetRoomList message to itself so that it can join another room.
Other client actors who are still in the game will receive a
notification that the host has left the game and to join another room.

# 3. Scalability & Reliability of System

## Scalability of the System

![](media\image6.png){width="6.1363615485564305in"
height="5.585662729658793in"}

The purpose of the server actor is to send the members list to each
client actor. The server actor will broadcast the members list whenever
there are any changes in the members list.

![](media\image7.png){width="5.3506091426071745in"
height="5.144816272965879in"}

Within the game room, one of the client actors will act as the game
host. Each client actor will directly communicate with each other
regarding any game updates. For instance, each client will broadcast any
changes made to the bet amount to all clients. The host will coordinate
the game and handle starting a new round and distributing cards to each
client actor. The server does not participate in any actions incurred
for the duration of the game, once matchmaking has taken place.

By having the clients handle the game themselves, it reduces the burden
of the server as the server only needs to handle the lobby and keep
track of the members list, thereby improving the scalability of this
distributed system.

## Reliability of the System

Failure in any system is inevitable. This can be due to either human
errors or the system itself. To ensure that a system is deemed reliable,
the system must have the readiness in detecting and counteracting any
possible failures and performing error handling throughout the system
execution. Below are a handful of error handling mechanisms that were
implemented into the
system:![](media\image8.png){width="6.546875546806649in"
height="3.7083333333333335in"}

*[Diagram 1: Main lobby of the game]{.ul}*

Diagram 1 illustrates the GUI that will be displayed for the clients at
the very beginning when they have successfully connected to the server.
The buttons "Invite", "New", "Start", and "Leave" that are all
highlighted in red boxes have a slight faint yellow colour in comparison
with the "Join", "Exit", and "Volume" buttons. These fainted yellow
colour buttons are designed such that they are unclickable by the
clients at certain points of the game. This implementation is to prevent
any accidental or intentional clicks by the clients that will disrupt
and cause errors to the system. For instance, the "Start" button
function is to commence the game of Blackjack with all the available
clients in the game room. However, in diagram 1, there are no players in
the game room. Should the "Start" button be executable, then the game
will enter an error state as the game needed at least 1 client for the
game to work as intended.

*[Diagram 2: Pop up error message when the client tries to invite
oneself]{.ul}*![](media\image9.png){width="6.463542213473316in"
height="3.7083333333333335in"}

As illustrated in diagram 2, an error message is shown in a pop up
dialog box when a host tries to invite himself/herself into the same
game room. Similarly, an error message will also appear if the host
tries to invite a player that is already in the same game room. The
reason for this implementation is to prevent any possibility of
duplicate players joining the same server.

![](media\image10.png){width="6.46875in"
height="3.7291666666666665in"}*[Diagram 3: Pop up error message when
client tries to invite a player that is currently in game]{.ul}*

In diagram 3, when a player tries to invite another player in the lobby
that is already in an existing game server, the system will display a
pop up dialog box that will indicate that the player is currently busy
and is unavailable for invite. This mechanism is to prevent players who
are already in a game from receiving any new invitation from other
players. For example, if player Jordan is enjoying a game of Blackjack
with two of his best friends, Albert and James, and a malicious player
decides to spam invites to Jordan, without the mechanism mentioned
earlier, Jordan screen will be bombarded with invitations, ruining the
overall game experience for Jordan.

# 4. Test Case Proposed & Result

## Test Cases

**Video Link: [[(Click here to be redirected to the
video)]{.ul}](https://drive.google.com/file/d/1Lc_HAVZK7XkjHGA9LpB_u3T2odP5bfzW/view?usp=sharing)**

+---------+--------------+--------------+--------------+--------------+
| **No.** | **Test       | **Test       | **Expected   | **Actual     |
|         | Case**       | Step**       | Result**     | Results**    |
+---------+--------------+--------------+--------------+--------------+
| 1\.     | Launch       | 1\. Run      | Application  | Application  |
|         | Blackjack    | client       | must be      | launched     |
|         | application  | application  | opened after | successfully |
|         |              |              | launching    |              |
|         |              | 2\. Input    | the          |              |
|         |              | port number  | application  |              |
+---------+--------------+--------------+--------------+--------------+
| 2\.     | Check        | 1\. Click on | Application  | Mute button  |
|         | f            | the Mute     | music should | performed as |
|         | unctionality | Button       | be muted and | intended     |
|         | on the Mute  |              | unmuted      |              |
|         | button in    |              | during each  |              |
|         | the lobby    |              | clicks       |              |
|         | screen       |              |              |              |
+---------+--------------+--------------+--------------+--------------+
| 3\.     | Check if     | 1\. Enter    | Application  | Created and  |
|         | client is    | name and     | should       | entered a    |
|         | able to      | click on     | transition   | game         |
|         | create and   | Join button  | from the     | successfully |
|         | enter a      |              | lobby screen |              |
|         | single       | 2\. Click on | to the game  |              |
|         | player game  | New button   | screen       |              |
|         | of Blackjack |              |              |              |
|         |              | 3\. Click on |              |              |
|         |              | Start button |              |              |
+---------+--------------+--------------+--------------+--------------+
| 4\.     | Play 3       | 1\. Click on | Application  | Blackjack    |
|         | rounds of    | Incre        | should be    | application  |
|         | Blackjack    | ase/Decrease | running      | game played  |
|         | alone to     | button       | smoothly     | successfully |
|         | ensure that  |              | with no      | for 3 rounds |
|         | all of the   | 2\. Click on | errors;      | with balance |
|         | game         | Confirm      | Client's     | of client    |
|         | functions    | button       | balance      | updated      |
|         | and logic    |              | should be    | correctly    |
|         | are working  | 3\. Wait for | adde         |              |
|         | as intended  | cards to be  | d/subtracted |              |
|         |              | distributed  | correctly    |              |
|         |              |              | according to |              |
|         |              | 4\. Click    | bet amount   |              |
|         |              | Hit button   |              |              |
|         |              | at least     |              |              |
|         |              | once         |              |              |
|         |              |              |              |              |
|         |              | 5\. Click    |              |              |
|         |              | Stand button |              |              |
|         |              | at least     |              |              |
|         |              | once         |              |              |
|         |              |              |              |              |
|         |              | 6\. Wait for |              |              |
|         |              | results and  |              |              |
|         |              | click Next   |              |              |
|         |              | Round button |              |              |
+---------+--------------+--------------+--------------+--------------+
| 5\.     | Check        | 1\. Click on | Application  | Mute button  |
|         | f            | the Mute     | music should | performed as |
|         | unctionality | button       | be muted and | intended     |
|         | on the Mute  |              | unmuted      |              |
|         | button in    |              | during each  |              |
|         | the game     |              | clicks       |              |
|         | screen       |              |              |              |
+---------+--------------+--------------+--------------+--------------+
| 6\.     | Check        | 1\. Click on | Application  | Client       |
|         | f            | the Leave    | should       | successfully |
|         | unctionality | button       | transition   | left the     |
|         | on the Leave |              | from the     | current game |
|         | button in    |              | game screen  |              |
|         | the game     |              | to the lobby |              |
|         | screen       |              | screen       |              |
+---------+--------------+--------------+--------------+--------------+
| 7\.     | Invite       | 1\. Click on | Both clients | Clients      |
|         | another      | the New      | should be in | joined the   |
|         | client to    | button       | the same     | same room    |
|         | the same     |              | game room    | successfully |
|         | room and     | 2\. Select   |              |              |
|         | start the    | the client's |              |              |
|         | game         | name and     |              |              |
|         |              | click on the |              |              |
|         |              | invite       |              |              |
|         |              | button       |              |              |
|         |              |              |              |              |
|         |              | 3\. Click on |              |              |
|         |              | the Start    |              |              |
|         |              | button       |              |              |
+---------+--------------+--------------+--------------+--------------+
| 8\.     | Play 1 round | 1\. Click on | Application  | Blackjack    |
|         | of Blackjack | Incre        | should be    | application  |
|         | with both    | ase/Decrease | running      | game played  |
|         | clients to   | button       | smoothly     | successfully |
|         | ensure that  |              | with no      | for 1 round  |
|         | all of the   | 2\. Click on | errors; Both | with balance |
|         | game         | Confirm      | Client's     | of both      |
|         | functions    | button       | balance      | client       |
|         | and logic    |              | should be    | updated      |
|         | are working  | 3\. Wait for | adde         | correctly    |
|         | as intended  | cards to be  | d/subtracted |              |
|         |              | distributed  | correctly    |              |
|         |              |              | according to |              |
|         |              | 4\. Click    | bet amount   |              |
|         |              | Hit button   |              |              |
|         |              | at least     |              |              |
|         |              | once         |              |              |
|         |              |              |              |              |
|         |              | 5\. Click    |              |              |
|         |              | Stand button |              |              |
|         |              | at least     |              |              |
|         |              | once         |              |              |
|         |              |              |              |              |
|         |              | 6\. Wait for |              |              |
|         |              | results and  |              |              |
|         |              | click Next   |              |              |
|         |              | Round button |              |              |
+---------+--------------+--------------+--------------+--------------+

## Internet Connectivity

As this distributed system is using Akka cluster, all clients must be
forwarded so that the clients can send and receive messages among each
other. We have used port 2222 for the server, while clients can choose
any ports as long it can be successfully port forwarded.

We have used a free dynamic DNS service by the company No-IP so that
clients can connect to the server without hard-coding the public IPv4
address. The computer which hosts the server would need to download a
client from No-IP which will dynamically update the IPv4 address with
the hostname (bjgame.ddns.net) as the IPv4 address may change over time.

![](media\image11.png){width="4.755208880139983in"
height="2.8653182414698164in"}

The screenshot above confirms that when pinging "bjgame.ddns.net", it
leads to the correct IPv4 address of the server.

![](media\image12.png){width="6.5in" height="2.9722222222222223in"}

Jordan's view of the application.

![](media\image13.png){width="6.5in" height="3.0694444444444446in"}

Chris' view of the application.

This shows that Jordan and Chris were able to connect to the same server
across the Internet.

## Additional Screenshots

1.  Lobby list and room list can handle members leaving or becoming
    unreachable.

![](media\image14.png){width="4.5in" height="3.2444192913385828in"}

View of Client 3333's Lobby with 2 members present: 3333 and 3334.

![](media\image15.png){width="4.5in" height="3.3075in"}

View of Client 3333's Lobby after member 3334 left.

2.  When new clients join an existing lobby, the server only sends the
    updated member list who are
    reachable.![](media\image16.png){width="4.5in" height="3.16125in"}

View of Client 3335's Lobby which consists of member 3333 (joined
first), and member 3335 (joined third), while member 3334 (joined
second) is excluded.

3.  Clients can receive an invitation from a room host and choose to
    accept or decline.

![](media\image17.png){width="3.0in" height="1.4701990376202974in"}

Client 3335's pop-up message.

![](media\image18.png){width="3.0in" height="1.4752471566054244in"}

If the client accepts, the host receives an invitation accepted pop-up
message.

![](media\image19.png){width="3.0in" height="1.4752471566054244in"}

If the client rejects, the host receives an invitation rejected pop-up
message.

4.  Room list is updated when a member accepts an invitation from a game
    host.

![](media\image20.png){width="4.5in" height="3.2625in"}

View of Client 3333's room list after member 3335 accepts invitation.

5.  Room list is updated when a member leaves the room.

![](media\image21.png){width="4.5in" height="3.3211843832020995in"}

View of Client 3333's room list when member 3335 left the room.

6.  Client is correctly kicked from the room when the host leaves the
    room.

![](media\image22.png){width="4.5in" height="3.34125in"}

Before: Host 3333 and Member 3335 is in the same room.

![](media\image23.png){width="4.5in" height="3.2175in"}

After: Member 3335 receives a pop-up indicating that the host left the
room.

7.  Clients cannot invite other players who are busy (e.g.: in another
    room).

![](media\image24.png){width="6.0in" height="3.154741907261592in"}

Pop-up message indicating that the member being invited is busy.

8.  Host will wait for all game clients to confirm bet amount before
    distributing cards.

![](media\image25.png){width="6.0in" height="3.530546806649169in"}

9.  Bet amount is updated on all clients as bet amount is changed.

![](media\image26.png){width="6.0in" height="3.531645888013998in"}

Client 3333's view.

![](media\image27.png){width="6.0in" height="3.52380905511811in"}

Client 3335's view.

10. Indicate which client's turn and enable hit/stand button for the
    current client's turn.

![](media\image28.png){width="6.0in" height="3.5230260279965004in"}

Client 3333's view.

![](media\image29.png){width="6.0in" height="3.5172419072615924in"}

Client 3335's view.

11. Indicate whether clients won or lost to the dealer, update balance
    on all clients, enable next round button for host.

![](media\image30.png){width="6.0in" height="3.524271653543307in"}

Client 3333's view. Client 3333 lost 300 to the dealer, balance becomes
700. Next round button is enabled as Client 3333 is the game host.

![](media\image31.png){width="6.0in" height="3.525974409448819in"}

Client 3335's view. Client 3335 won 150 from the dealer, balance becomes
1150.

12. Round is restarted when the next round button is clicked.

![](media\image32.png){width="6.0in" height="3.5261537620297463in"}

Client 3333's view.

![](media\image33.png){width="6.0in" height="3.5261537620297463in"}

Client 3335's view.

13. Clients in the game can handle sudden disconnection / leaving of
    clients.

![](media\image34.png){width="4.427083333333333in"
height="3.0729166666666665in"}

Client 3333's view. Since Client 3335 left the room, the current round
is immediately cancelled. Pop-up message indicating that Client 3335
left the room and to request the host to start a new round. Next round
button is enabled.

![](media\image35.png){width="2.7343755468066493in"
height="3.641905074365704in"}

Client 3335's name is strike-through to indicate disconnected status.

14. Clients in the game can handle sudden disconnection / leaving of the
    host.

![](media\image36.png){width="4.46875in" height="2.8645833333333335in"}

Clients receive a pop-up message indicating that the host has left the
room and to join another room.

# 5. Personal Reflections

## **Wong Chuen Ting (17035528)**

Concept:

One of the distributed system concepts that have been applied to the
Blackjack application is Scalability. Scalability of a system can be
defined as the ability to handle an indefinite increase in the amount of
load such as number of users to the system while simultaneously
maintaining the system's overall quality and performance. A highly
scalable distributed system can help to prevent any bottlenecks from
occurring that may hinder the performance of the system. In this
scenario, players using the system will not be affected by any
performance loss or issues during highly congested hours.

Application:

In a typical client-server architecture, one of the downsides of the
architecture is traffic congestion. This occurs when there are multiple
clients requesting for resources from the server simultaneously. As a
result, the server gets overloaded with requests, causing traffic
congestion. For this Blackjack application, it also uses the
client-server architecture. However, the server is designed in a way
such that it is only responsible for keeping track of the online players
and initialising the game. The client on the other hand is responsible
for updating other players' information without the need of the server.
As a result, the server will be less likely to face any traffic
congestion due to the limited number of tasks it handles.

Difficulties:

One of the difficulties faced during this assignment is understanding
and implementing the Akka framework into the system. As stated in the
assignment requirements, the distributed system should be developed
using Akka as the middleware to support the development of the system.
With the unfamiliarity of Akka framework together with the lack of
knowledge in Scala Programming language, the coding of the system is
mainly done by Nicholas Ng and Jordan Kee to ensure the system functions
smoothly and efficiently due to the time constraint.

Strengths:

-   Highly scalable system that can accommodate large numbers of
    lobby/games concurrently due to the architecture design.

-   Players can play with other players geographically through the
    Internet

-   Connected players can still continue to play with each other if the
    server is disrupted/down

Weakness:

-   Records of the past clients' statistics and scores are not recorded
    and stored. Once the player leaves the game, the player's
    information is deleted.

-   Application window is unable to increase/decrease in size.

-   If the server is terminated during a game, the clients connected
    will not be notified.

Contribution:

  ----------------------- ------------------
  **Name**                **Contribution**
  Wong Chuen Ting         100%
  Jordan Kee Zheng Feng   100%
  Nicholas Ng Yee Jet     100%
  Gan Wei Qi              100%
  ----------------------- ------------------

## **Jordan Kee Zheng Feng (17029752)**

Concept:

We were tasked to design a distributed system that is scalable and
reliable. Scalable refers to the ability of the system to adapt to
increased workload. Reliable refers to the ability of the system to
perform its intended function without failure.

Application:

In this case, we used Scala with ScalaFX and AKKA middleware to create a
game to demonstrate our applied understanding of distributed systems. I
suggested to recreate Blackjack (a casino card game) as a game of this
nature would require distributed concepts and I'm also a big fan of this
game. We designed the game around the Client-server architecture, hence
we created two AKKA actors: Client and Server. The server is only used
to handle the lobby creation and matchmaking, it keeps track of online
players. There are two types of clients: the client host that is the
room leader, this client acts as a game instance's "master" and would
distribute cards and keep tabs on bet money. The second client is a
normal client that only plays the game.

Strengths:

-   Scalable - Since the main server is only in charge of the lobby and
    matchmaking, each game instance is actually hosted by the client
    host. This design allows many concurrent games to take place without
    burdening the main server. Hundreds of games could be played
    simultaneously in theory with no issue.

-   Reliable - AKKA middleware allows the game to handle unexpected
    behavior such as clients leaving halfway through the game or
    disconnecting due to system failure.

-   Polished - Due to the well-designed graphics created in Photoshop,
    and custom-made voiceover lines recorded by myself, the game feels
    very polished where it does not feel like a prototype and could pass
    off easily as a deploy-ready game.

Weaknesses:

-   Only supports 3 players in 1 game - Since each player's hand has to
    be displayed, the ScalaFX graphical elements are hardcoded. The game
    cannot support a fourth player in its current state unless the
    ScalaFX FXML is edited and the code would have to be updated to
    include the new elements such as ImageViews, TextFields, etc.

-   Does not support window scaling - Due to time constraints and scope,
    we are focused on proving our applied understanding of distributed
    systems. We designed the ScalaFX FXMLs to be of fixed window-size
    from the very start of development, to save time.

Contribution:

  ----------------------- ------------------
  **Name**                **Contribution**
  Wong Chuen Ting         100%
  Jordan Kee Zheng Feng   100%
  Nicholas Ng Yee Jet     100%
  Gan Wei Qi              100%
  ----------------------- ------------------

## **Nicholas Ng Yee Jet (17003625)**

Concepts:

To apply scalability in our distributed system, we have used 2 actors
which are the server and client actors where the server handles the
lobby sessions while the client handles the game sessions. In terms of
reliability, we made sure that the system is able to function well even
in the event of client omission.

Difficulties:

-   During our consultation with Dr. Chin, we were told to implement 4
    actors which are server actor, client actor, game client actor, and
    game host actor. However, due to our inexperience, we decided to
    settle with 2 actors which are the server actor and client actor.
    The client actor can act as both the host and player in the game,
    which caused the code to be quite complicated.

Strengths:

-   A distributed system game that can function over the internet (with
    port forwarding)

-   A scalable distributed system that can handle more clients in the
    lobby as clients would host the game and play among each other.

-   A GUI application that utilises ScalaFX through the use of FXML and
    CSS to make the design of the game look pleasing and polished.

-   Sound effects to add more interactivity with the game.

Weaknesses:

-   Troublesome to adjust the bet amount in bigger quantities. We
    allowed players to adjust through the increase and decrease button
    to make the code simpler.

-   Lack of scoreboard to keep track of player's performance.

Contribution:

  ----------------------- ------------------
  **Name**                **Contribution**
  Wong Chuen Ting         100%
  Jordan Kee Zheng Feng   100%
  Nicholas Ng Yee Jet     100%
  Gan Wei Qi              100%
  ----------------------- ------------------

## **Gan Wei Qi (17032418)**

Concepts applied:

Regarding the proposed system, it is understood that the design of the
distributed system should be scalable and reliable in order to support
the influx of clients using the system while constantly adapting to the
increasing workload of the network, and handle exceptions that may occur
while the system is running to ensure a smooth workflow with minimum
risk of system crashing.

Difficulties faced in this project:

-   There were port-forwarding issues present, and that I was unable to
    port-forward because of the lack of access to my router. This led to
    minor delays in systems testing and more effort was needed to find
    alternatives to bypass the port-forwarding issues.

-   It was difficult to understand how to implement the actors in the
    system, which led to us deconstructing the code to better understand
    how the system would work. We eventually decided to have a meeting
    with Dr. Chin after going through the akka documentation and still
    not understanding how the actors work, with Dr. Chin explaining to
    us the flow of information and behaviour between actors in the
    system and how to make the system scalable.

Strengths:

-   The proposed system implements distributed systems which allow the
    game to be hosted through the internet

-   The proposed system uses ScalaFX to make the interface, which allows
    for a visual representation of the system application, along with
    sound effects and images for better immersion

-   The proposed system is scalable due to how the clients can handle
    each other with minimal overhead in the server

Weaknesses:

-   The application window is fixed which may affect some clients

-   Cheating may occur as each client handles their own game state
    instead of the server

Contribution:

  ----------------------- ------------------
  **Name**                **Contribution**
  Wong Chuen Ting         100%
  Jordan Kee Zheng Feng   100%
  Nicholas Ng Yee Jet     100%
  Gan Wei Qi              100%
  ----------------------- ------------------
