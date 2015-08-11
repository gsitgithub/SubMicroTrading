6.6 Message Sequence Number

The MsgSeqNum (34) in the request header must increment with each message sent by the participant to the gateway, 
starting with the Session Logon message as sequence number 1.
The Eurex ETI will echo the participant’s MsgSeqNum (34) of the request header in the corresponding response header.
In case of any unexpected sequence numbers, sequence number gaps, or duplicate sequence numbers, 
the request message will be rejected with a sequence number error, and the session will be disconnected.
Note: There is no recovery mechanism for message sequence numbers in the Eurex ETI. 
All participant connections (including a reconnection after a disconnection) are considered “new,�? and all Session
Logon requests are expected to contain the message sequence number 1.


6.7.1 Application Message Identifier : ApplMsgID

All recoverable session and listener data sent by Eurex ETI will provide an application message identifier, ApplMsgID (28704), 
to uniquely identify order and quote related data sent by the gateway.
With the help of the application message identifier, the participant is able to ask for a retransmission of recoverable order/quote data.
The same application message identifier is also provided in the Listener Broadcast (standard order drop copy).
The ApplMsgID (28704) has the following characteristics:

. It is unique per partition and business day.
. It is ascending during a business day until end-of-stream.
. Gap detection is not possible.
. It does not start at any particular number.
. Consists of 16 bytes, ordered with the highest significant byte first (as in big endian).


6.7.2 Application Message Sequence Number : ApplSeqNum

Eurex ETI will assign an application message sequence number, the ApplSeqNum (1181), to messages related to Trade Notification, News and Risk Control 
(Risk Notification and Entitlement Notification).
The ApplSeqNum (1181) has the following characteristics:

. The first message will be the message sequence number 1.
. It is ascending during a business day until end-of-stream (Trade Notification).
. The message sequence will be gapless and allows gap detection.
. Trade notification: unique per business day, partition and business unit.
. News: unique per market.
. Risk Control: unique per business unit.

