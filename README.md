##Architettura

- Ogni nodo fa partire un Server e un client
- Il client comunica unicamente con il suo server locale
- Il server gestisce lo stato del gioco, ovvero prende tutto quello che fa il suo client locale 
e lo propaga agli altri server. Inoltre rimane in ascolto per applicare le modifiche effettuate dagli altri client.


##Connessione
- In uno stato iniziale supponiamo di non avere nessun nodo attivo. Il primo nodo X parte, e fa partire il suo server
e il suo client. In questo momento può cominciare a giocare da solo. Un altro giocatore Y decide che vuol giocare con 
  lui. In questo caso il server di Y, conoscendo l'ip e porta di X, apre una connessione verso Y.
  Y comunica a X tutti i server attualmente attivi e avvia una connessione con tutti i server. Ora anche Y giocare con X.
  Chiaramente Y riceve lo stato attuale del gioco.