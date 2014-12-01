

/* First created by JCasGen Sat Oct 18 19:40:19 EDT 2014 */
package edu.cmu.lti.oaqa.type.retrieval;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** A search result where the candidate answer is obtained as part of the search process and saved in the text field of the search result.
 * Updated by JCasGen Sun Nov 30 03:03:33 EST 2014
 * XML source: /Users/seven/git/project-team05/project-team05/src/main/resources/type/OAQATypes.xml
 * @generated */
public class AnswerSearchResult extends SearchResult {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(AnswerSearchResult.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected AnswerSearchResult() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AnswerSearchResult(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AnswerSearchResult(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
  //*--------------*
  //* Feature: answer

  /** getter for answer - gets Exact answer

   * @generated
   * @return value of the feature 
   */
  public String getAnswer() {
    if (AnswerSearchResult_Type.featOkTst && ((AnswerSearchResult_Type)jcasType).casFeat_answer == null)
      jcasType.jcas.throwFeatMissing("answer", "edu.cmu.lti.oaqa.type.retrieval.AnswerSearchResult");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AnswerSearchResult_Type)jcasType).casFeatCode_answer);}
    
  /** setter for answer - sets Exact answer
 
   * @generated
   * @param v value to set into the feature 
   */
  public void setAnswer(String v) {
    if (AnswerSearchResult_Type.featOkTst && ((AnswerSearchResult_Type)jcasType).casFeat_answer == null)
      jcasType.jcas.throwFeatMissing("answer", "edu.cmu.lti.oaqa.type.retrieval.AnswerSearchResult");
    jcasType.ll_cas.ll_setStringValue(addr, ((AnswerSearchResult_Type)jcasType).casFeatCode_answer, v);}    
  }

    