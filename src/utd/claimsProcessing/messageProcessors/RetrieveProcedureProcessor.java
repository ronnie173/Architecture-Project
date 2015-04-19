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

import utd.claimsProcessing.dao.ProcedureDAO;
import utd.claimsProcessing.domain.Claim;
import utd.claimsProcessing.domain.ClaimFolder;
import utd.claimsProcessing.domain.Procedure;
import utd.claimsProcessing.domain.RejectedClaimInfo;

/**
 * A message processor responsible for retrieving the Procedure identified by
 * the Claim from the ProcedureDAO. The retrieved policy is attached to the
 * ClaimFolder before passing to the next step in the process.
 */
public class RetrieveProcedureProcessor extends MessageProcessor implements
		MessageListener {
	private final static Logger logger = Logger
			.getLogger(RetrieveProcedureProcessor.class);

	private MessageProducer producer;

	public RetrieveProcedureProcessor(Session session) {
		super(session);
	}

	public void initialize() throws JMSException {
		Queue queue = getSession().createQueue(QueueNames.retrieveProcedure);
		producer = getSession().createProducer(queue);
	}

	public void onMessage(Message message) {
		logger.debug("RetrieveProcedureProcessor ReceivedMessage");

		try {
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder) object;
			String procedureID = claimFolder.getProcedure().getID();
			Procedure procedure = ProcedureDAO.getSingleton()
					.retrieveProcedure(procedureID);
			if (procedure == null) {
				Claim claim = claimFolder.getClaim();
				RejectedClaimInfo rejectedClaimInfo = new RejectedClaimInfo(
						"Procedure Not Found: " + procedureID);
				claimFolder.setRejectedClaimInfo(rejectedClaimInfo);

				if (!StringUtils.isBlank(claim.getReplyTo())) {
					rejectedClaimInfo.setEmailAddr(claim.getReplyTo());
				}

				rejectClaim(claimFolder);
			} else {
				logger.debug("Procedure Member: " + procedure.getID());

				claimFolder.setProcedure(procedure);

				Message claimMessage = getSession().createObjectMessage(
						claimFolder);
				producer.send(claimMessage);
				logger.debug("Finished Sending Procedure : "
						+ procedure.getID());
			}
		} catch (Exception ex) {
			logError(
					"RetrieveProcedureProcessor.onMessage() " + ex.getMessage(),
					ex);
		}
	}
}
