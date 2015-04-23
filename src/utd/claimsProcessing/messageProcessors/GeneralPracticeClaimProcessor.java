package utd.claimsProcessing.messageProcessors;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.log4j.Logger;

import utd.claimsProcessing.domain.ClaimFolder;
import utd.claimsProcessing.domain.ProcedureCategory;

public class GeneralPracticeClaimProcessor extends AbstractProcedureProcessor
		implements MessageListener {

	public GeneralPracticeClaimProcessor(Session session) {
		super(session);
	}

	public void initialize() throws JMSException {
		Queue queue = getSession().createQueue(QueueNames.payClaim);
		paymentProducer = getSession().createProducer(queue);

		queue = getSession().createQueue(QueueNames.denyClaim);
		denyProducer = getSession().createProducer(queue);
	}

	private final static Logger logger = Logger
			.getLogger(GeneralPracticeClaimProcessor.class);

	public void onMessage(Message message) {
		logger.debug("GPClaimProcessor ReceivedMessage");

		try {
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder) object;

			Message claimMessage = getSession()
					.createObjectMessage(claimFolder);
			logger.debug("Claim folder" + claimMessage);

			if (validatePolicy(claimFolder)
					&& validateProcedure(claimFolder,
							ProcedureCategory.GeneralPractice)) {
				paymentProducer.send(claimMessage);
			}

		} catch (Exception ex) {
			logError("GPClaimProcessor.onMessage() " + ex.getMessage(), ex);
		}

	}

}