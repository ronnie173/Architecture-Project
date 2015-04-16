package utd.claimsProcessing.messageProcessors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import utd.claimsProcessing.domain.Claim;
import utd.claimsProcessing.domain.ClaimFolder;
import utd.claimsProcessing.domain.ProcedureCategory;
import utd.claimsProcessing.domain.RejectedClaimInfo;

/**
 * A message processor responsible for retrieving the Procedure identified by the
 * Claim from the ProcedureDAO. The retrieved policy is attached to the ClaimFolder
 * before passing to the next step in the process.
 */
public class RouteClaim extends MessageProcessor implements MessageListener{
	private final static Logger logger = Logger
			.getLogger(RetrieveProcedureProcessor.class);

	private MessageProducer producer;

	public RouteClaim(Session session) {
		super(session);
	}

	public void initialize() throws JMSException {
		
	}

	public void onMessage(Message message) {
		logger.debug("RouteClaim ReceivedMessage");

		try {
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder) object;
			ProcedureCategory category = claimFolder.getProcedure().getProcedureCategory();
			
			if (category == null) {
				Claim claim = claimFolder.getClaim();
				RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo(
						"Procedure Not Routed: " + category);
				claimFolder.setRejectedClaimInfo(rejectedClaimInfo);

				if (!StringUtils.isBlank(claim.getReplyTo())) {
					rejectedClaimInfo.setEmailAddr(claim.getReplyTo());
				}

				rejectClaim(claimFolder);
			} 
			else {
				logger.debug("Procedure Category: " + category);
			
				Message claimMessage = getSession().createObjectMessage(
						claimFolder);
				Queue queue = null;
				//set destination queue depending on Procedure Category
				switch (category){
				case GeneralPractice:	queue = getSession().createQueue(QueueNames.processGPClaim);
										break;
				case Dental:	queue = getSession().createQueue(QueueNames.processDentalClaim);
										break;
				case Radiology:	queue = getSession().createQueue(QueueNames.processRadiologyClaim);
										break;
				case Optometry:	queue = getSession().createQueue(QueueNames.processOptometryClaim);
										break;
							
				}
				producer = getSession().createProducer(queue);
				producer.send(claimMessage);
				logger.debug("Finished Sending Claim to : " + category);
			}
		} catch (Exception ex) {
			logError(
					"RetrieveProcedureProcessor.onMessage() " + ex.getMessage(),
					ex);
		}
	}
}

