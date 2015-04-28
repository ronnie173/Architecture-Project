package utd.claimsProcessing.messageProcessors;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.log4j.Logger;

import utd.claimsProcessing.domain.ClaimFolder;
import utd.claimsProcessing.domain.ProcedureCategory;

public class DentalClaimProcessor extends AbstractProcedureProcessor implements
		MessageListener {

	public DentalClaimProcessor(Session session) {
		super(session);
	}

	public void initialize() throws JMSException {
		Queue queue = getSession().createQueue(QueueNames.payClaim);
		paymentProducer = getSession().createProducer(queue);

		queue = getSession().createQueue(QueueNames.denyClaim);
		denyProducer = getSession().createProducer(queue);
	}

	private final static Logger logger = Logger
			.getLogger(DentalClaimProcessor.class);
	// private AbstractProcedureProcessor processor;
	private MessageProducer producer;

	public void onMessage(Message message) {
		logger.debug("DentalClaimProcessor ReceivedMessage");

		try {
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder) object;

			Message claimMessage = getSession()
					.createObjectMessage(claimFolder);

			if (validatePolicy(claimFolder)
					&& this.validateProcedure(claimFolder,
							ProcedureCategory.Dental)) {
				producer.send(claimMessage);
			}

		} catch (Exception ex) {
			logError("DentalClaimProcessor.onMessage() " + ex.getMessage(), ex);
		}

	}

}
