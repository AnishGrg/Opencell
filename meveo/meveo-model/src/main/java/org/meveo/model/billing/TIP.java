/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.model.billing;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.management.InvalidAttributeValueException;


/*
 * @author Sebastien Michea
 * @created May 20, 2011
 * 
 * This class represents an inter-bank title
 * it was created from "NORME CFONB sur les TIPS"
 * 
 */
public class TIP {
        private static String num="0123456789";
        private static String alphanum=" ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
        private static SimpleDateFormat invoiceDateSDF = new SimpleDateFormat("yyyyMMdd");
        private static NumberFormat NF = NumberFormat.getNumberInstance();
        static {
            NF.setMinimumIntegerDigits(1);
            NF.setMaximumIntegerDigits(6);
            NF.setMinimumFractionDigits(2);
            NF.setMaximumFractionDigits(2);
        }
        
        private static GregorianCalendar cal = new GregorianCalendar();
        
        public TIP(String codeCreancier, String codeEtablissementCreancier,String codeCentre,
        BankCoordinates coordonneesBancaires,String customerAccountCode,long invoiceId,
        Date invoiceDate,Date invoiceDueDate,
        BigDecimal netToPay) throws InvalidAttributeValueException {
            
            if(codeCreancier==null || codeCreancier.length()!=6){
                throw new InvalidAttributeValueException("codeCreancier must be 6 characters");
            }
            this.codeCreancier=codeCreancier;
            
            if(codeEtablissementCreancier==null || codeEtablissementCreancier.length()<4){ 
                throw new InvalidAttributeValueException("codeEtablissementCreancier must be 4 characters at least");
            }
            this.codeEtablissementCreancier=codeEtablissementCreancier.substring(codeEtablissementCreancier.length()-4, 4);

            if(codeCentre==null || codeCentre.length()!=2){
                throw new InvalidAttributeValueException("codeCentre must be 2 characters");
            }
            this.codeCentre=codeCentre;
            
            //pas besoin d'indiquer l'échéance si on n'a pas affaire à des tip à échéance --> mettre les 6 caractères de gauche à blanc
            //cal.setTime(invoiceDueDate);
            //dateEcheance=cal.get(GregorianCalendar.YEAR)%10+""+cal.get(GregorianCalendar.DAY_OF_YEAR);
            dateEcheance="";
            
            if(netToPay==null){
                throw new InvalidAttributeValueException("netToPay cannot be null");
            }
            if(netToPay.doubleValue()<0){
                throw new InvalidAttributeValueException("netToPay cannot be negative");
            }
            if(netToPay.doubleValue()>1000000L){
                throw new InvalidAttributeValueException("netToPay cannot be > 1 000 000");
            }
            String netToPayString=NF.format(netToPay.doubleValue());
            montant="";
            for(int i=0;i<netToPayString.length();i++){
                if(num.indexOf(netToPayString.substring(i,i+1))>-1){
                    montant+=netToPayString.substring(i,i+1);
                } 
            }
            
            if(customerAccountCode==null || customerAccountCode.length()==0){
                throw new InvalidAttributeValueException("customerAccountCode cannot be empty");
            }

            if(invoiceDate==null){
                throw new InvalidAttributeValueException("invoiceDate cannot be null");
            }
            referenceOperation=invoiceId+"";
            referenceOperation=leftPad(referenceOperation,15,"0");
            
            numeroFormule="";
            if(numeroFormule.length()<11){
                numeroFormule=leftPad(numeroFormule,11,"0");
            } else if(numeroFormule.length()>11){
                numeroFormule=numeroFormule.substring(numeroFormule.length()-11,11);
            }
            
            if(coordonneesBancaires==null){
                throw new InvalidAttributeValueException("coordonneesBancaires cannot be null");
            }
            
            intituleCompteDebiteur=coordonneesBancaires.getAccountOwner();
            if(intituleCompteDebiteur==null){
                intituleCompteDebiteur="";
            }
            if(intituleCompteDebiteur.length()>18){
                intituleCompteDebiteur=intituleCompteDebiteur.substring(intituleCompteDebiteur.length()-18);
            }
            
            
            intituleCompteDebiteur = intituleCompteDebiteur.toUpperCase(Locale.FRENCH);
            intituleCompteDebiteur = Normalizer.normalize(intituleCompteDebiteur, Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");
            
            String temp="";
            for(int i=0;i<intituleCompteDebiteur.length();i++){
                if(alphanum.indexOf(intituleCompteDebiteur.substring(i,i+1))>-1){
                    temp+=intituleCompteDebiteur.substring(i,i+1);
                }
            }
            intituleCompteDebiteur= temp+"                  ".substring(temp.length());
            
            if(coordonneesBancaires.getBankCode()==null || coordonneesBancaires.getBankCode().length()!=5){
                throw new InvalidAttributeValueException("coordonneesBancaires.getBankCode must be 5 characters");
            }
            codeEtablissement=coordonneesBancaires.getBankCode();
            
            if(coordonneesBancaires.getBranchCode()==null || coordonneesBancaires.getBranchCode().length()!=5){
                throw new InvalidAttributeValueException("coordonneesBancaires.getBranchCode must be 5 characters");
            }
            codeGuichet=coordonneesBancaires.getBranchCode();
            
            if(coordonneesBancaires.getAccountNumber()==null || coordonneesBancaires.getAccountNumber().length()!=11){
                throw new InvalidAttributeValueException("coordonneesBancaires.getAccountNumber must be 11 characters");
            }
            numeroCompte=coordonneesBancaires.getAccountNumber();
            
            if(coordonneesBancaires.getKey()==null || coordonneesBancaires.getKey().length()!=2){
                throw new InvalidAttributeValueException("coordonneesBancaires.getKey must be 2 characters");
            }
            cleRib=coordonneesBancaires.getKey();
            
            
            
        }
    
    
    
        String numeroFormule;
        String intituleCompteDebiteur;
        String codeEtablissement;
        String codeGuichet;
        String numeroCompte;
        String cleRib;

        private static String[] CONVERT=new String[4];
        
        static {
            CONVERT[0]="ABCDEFGHI";
            CONVERT[1]="JKLMNOPQR";
            CONVERT[2]=" STUVWXYZ";
            CONVERT[3]="123456789";
        }
        
        public String getLigneOptiqueHaute(){
            return getEnsemble6()+getEnsemble5()+getEnsemble4();
        }
        
        String getEnsemble6(){
            return numeroFormule+getCle5()+" ";
        }
        
        
        //we assume numeroFormule contains only capital letters or digit
        int getCle5(){
            int result = 0;
            String replacedFormula="";
            for(int i=0;i<numeroFormule.length();i++){
                int index = -1;
                String c=numeroFormule.substring(i,i+1);
                for(int j=0;j<CONVERT.length;j++){
                    index = CONVERT[j].indexOf(c);
                    if(index>-1){
                        break;
                    }
                }
                if(index>-1){
                    replacedFormula+=(index+1);
                } else {
                    replacedFormula+="0";//Beware this method is very permissive
                }
            }
            long tempResult = Long.parseLong(replacedFormula);
            result=(int) (11-tempResult%11);
            if(result==11){
                result=1;
            }
            if(result==10){
                result=0;
            }
            return result;
        }
        
        String getEnsemble5(){
            return intituleCompteDebiteur+"  ";//FIXME : SHOULD BE ON DEFINITE LENGTH
        }
        
        String getEnsemble4(){
            return codeEtablissement+codeGuichet+numeroCompte+cleRib;
        }
        
        private static final byte ZERO = "0".getBytes()[0];
        
        String dateEcheance;
        String reservee="";
        String codeCreancier;
        String codeEtablissementCreancier;
        String referenceOperation;
        String codeDocument="9";
        String codeNature="8";
        String codeCentre;
        String montant;
        
        public String getLigneOptiqueBasse(){
            return getEnsemble3()+getEnsemble2()+getEnsemble1();
        }
        
        String getEnsemble3(){
            String format = String.format("%%0%dd", 2);
            return dateEcheance+reservee+codeCreancier+codeEtablissementCreancier+String.format(format,getCle3())+" ";
        }
        
        int getCle3(){
            return getCle(dateEcheance+reservee+codeCreancier+codeEtablissementCreancier);
        }
        
        String getEnsemble2(){
            String format = String.format("%%0%dd", 2);
            return String.format(format,getCle2())+referenceOperation+codeDocument;
        }
        
        int getCle2(){
            return getCle(referenceOperation+codeDocument);
        }
        
        String getEnsemble1(){          
            String montantPadde = leftPad(montant,8," ");
            String format = String.format("%%0%dd", 2);
            return String.format(format,getCle1())+codeNature+codeCentre+" "+montantPadde;
        }
        
        //we assume montant.length<9
        int getCle1(){
            //System.out.println("montant "+montant);
            String montantPadde = leftPad(montant,8,"0");
            //System.out.println("cle1 sur "+codeNature+codeCentre+montantPadde);
            return getCle(codeNature+codeCentre+montantPadde);
        }
        
        //we assume param only contain digits
        private int getCle(String param){
            int result = 0;
            byte[] digits = param.getBytes();
            int rang=1;
            for(int i=(digits.length-1);i>=0;i--){
                int digit = digits[i]-ZERO;
                result+=digit*rang;
                rang++;
                result%=100;
            }
            return result;
        }
        
        private String leftPad(String in,int length,String pad){
            String result=in;
            if(in.length()<length){
                StringBuffer padding = new StringBuffer();
                for(int i=in.length();i<length;i++){
                    padding.append(pad);
                }
                result=padding+in;
            }
            return result;
        }
        
        
}
