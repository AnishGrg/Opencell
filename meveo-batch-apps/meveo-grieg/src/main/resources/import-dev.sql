
insert into COM_MESSAGE_TEMPLATE (ID,PROVIDER_ID,VERSION,MEDIA,CREATED,DISABLED,CODE,TAG_END,TAG_START,TYPE,HTMLCONTENT,SUBJECT,TEXTCONTENT)
values (1,1,0,'EMAIL',SYSDATE,0,'PART_RAPPEL','>','#<','DUNNING',null,'RAPPEL #<LETTER_DATE>','Monsieur,'||CHR(9) || CHR(10)||CHR(9) || CHR(10)||' A ce jour et sauf erreur de notre part, vous restez nous devoir la somme de #<AMOUNT_WITH_TAX>�.'||CHR(9) || CHR(10)||' Si vous avez effectu� votre paiement r�cemment, nous vous prions de ne pas tenir compte de cette relance.'||CHR(9) || CHR(10)||'S''il s''agit d''un oubli, nous vous invitons � r�gler votre facture d�s aujourd''hui par tout moyen de paiement � votre convenance.'||CHR(9) || CHR(10)||' A d�faut de r�glement dans un d�lai de 15 jours, une p�nalit� de 10,00 � minimum viendra s''ajouter � votre solde d�.'||CHR(9) || CHR(10)||'En cas de difficult� ou pour toute autre raison, n''h�sitez pas � contacter rapidement notre service client�le aux num�ros et horaires pr�cis�s en haut � gauche de cette lettre.'||CHR(9) || CHR(10)||'Dans l''attente de votre r�glement, nous vous prions d''agr�er, Monsieur, l''exression de nos sentiments d�vou�s.'||CHR(9) || CHR(10)||CHR(9) || CHR(10)||'Le Service Client Dolce � "');
insert into COM_MESSAGE_TEMPLATE (ID,PROVIDER_ID,VERSION,MEDIA,CREATED,DISABLED,CODE,TAG_END,TAG_START,TYPE,HTMLCONTENT,SUBJECT,TEXTCONTENT)
values (2,1,0,'EMAIL',SYSDATE,0,'PART_MISDEM','>','#<','DUNNING',null,'MISE EN DEMEURE #<LETTER_DATE>','Cher #<CUSTOMER_NAME>,'||CHR(9) || CHR(10)||'Vous n''avez pas r�gl� votre facture du #<INVOICE_DATE>, nous devez #<AMOUNT_WITH_TAX> euros � ce jour.' );
insert into COM_MESSAGE_TEMPLATE (ID,PROVIDER_ID,VERSION,MEDIA,CREATED,DISABLED,CODE,TAG_END,TAG_START,TYPE,HTMLCONTENT,SUBJECT,TEXTCONTENT)
values (3,1,0,'EMAIL',SYSDATE,0,'PART_DERNAV','>','#<','DUNNING','<html><h1>Cher #<CUSTOMER_NAME>,</h1><p>Vous n''avez pas r�gl� votre facture du <b>#<INVOICE_DATE></b>, nous devez #<AMOUNT_WITH_TAX> euros � ce jour.</p></html>','Votre facture du #<INVOICE_DATE>',null );

insert into COM_SENDER_CONFIG (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,CODE,MEDIA,SMTP_HOST,SMTP_PORT,LOGIN,PASSWORD,DELAY_MIN,DEFAULT_FROM_EMAIL,DEFAULT_REPLY_EMAIL)
values (1,0,SYSDATE,0,1,'MANATY SMTP','EMAIL','zimbra.manaty.net','25','projetmeveo','projetMEVEO',5000,'toto@gmail.com','reply@gmail.com');

delete from COM_MSG_VAR_VALUE;
delete from COM_MESSAGE;
delete from COM_CAMPAIGN;

insert into COM_CAMPAIGN (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,CODE,SCHEDULE_DATE,STATUS)
values (1,0,SYSDATE,0,1,'TEST',sysdate,'SCHEDULED');

insert into COM_MESSAGE (ID,VERSION,PROVIDER_ID,TEMPLATECODE,CAMPAIGN_ID,MEDIA,STATUS)
values (1,0,1,'PART_RAPPEL',1,'EMAIL','WAITING');

insert into COM_MSG_VAR_VALUE (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,MESSAGE,CODE,VALUE)
values (1,0,sysdate,0,1,1,'RECIPIENT_ADDRESS','smichea@gmail.com');
insert into COM_MSG_VAR_VALUE (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,MESSAGE,CODE,VALUE)
values (2,0,sysdate,0,1,1,'CUSTOMER_NAME','M SEBASTIEN MICHEA');
insert into COM_MSG_VAR_VALUE (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,MESSAGE,CODE,VALUE)
values (3,0,sysdate,0,1,1,'LETTER_DATE','01/01/2011');
insert into COM_MSG_VAR_VALUE (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,MESSAGE,CODE,VALUE)
values (4,0,sysdate,0,1,1,'AMOUNT_WITH_TAX','136,2');

