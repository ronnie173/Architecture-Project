package utd.claimsProcessing.messageProcessors;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import utd.claimsProcessing.domain.Claim;
import utd.claimsProcessing.domain.ClaimFolder;
import utd.claimsProcessing.domain.Payment;
import utd.claimsProcessing.domain.Policy;
import utd.claimsProcessing.domain.ProcedureCategory;
import utd.claimsProcessing.domain.RejectedClaimInfo;

public class GeneralPracticeClaimProcessor extends AbstractProcedureProcessor implements MessageListener {

	public GeneralPracticeClaimProcessor(Session session) {
		super(session);
	}

	public void initialize() throws JMSException {
		Queue queue = getSession().createQueue(QueueNames.payClaim);
		producer = getSession().createProducer(queue);
	}
	
	private final static Logger logger = Logger.getLogger(GeneralPracticeClaimProcessor.class);
	//private AbstractProcedureProcessor processor;
	private MessageProducer producer;

	public void onMessage(Message message) {
		logger.debug("GPClaimProcessor ReceivedMessage");

		try {
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder) object;		
			
			Message claimMessage = getSession().createObjectMessage(claimFolder);
			
			if (validatePolicy(claimFolder)
					&& this.validateProcedure(claimFolder,	ProcedureCategory.GeneralPractice)) {		
				producer.send(claimMessage);
			}				
			
		}catch (Exception ex) {
			logError("GPClaimProcessor.onMessage() " + ex.getMessage(),ex);
		}

	}

}