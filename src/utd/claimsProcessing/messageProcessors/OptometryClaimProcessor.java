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

public class OptometryClaimProcessor extends AbstractProcedureProcessor
		implements MessageListener {

	public OptometryClaimProcessor(Session session) {
		super(session);
	}

	Queue queue = null;

	public void initialize() throws JMSException {

		Queue queue = getSession().createQueue(QueueNames.payClaim);
		paymentProducer = getSession().createProducer(queue);

		queue = getSession().createQueue(QueueNames.denyClaim);
		denyProducer = getSession().createProducer(queue);
	}

	private final static Logger logger = Logger
			.getLogger(OptometryClaimProcessor.class);

	private MessageProducer producer;

	public void onMessage(Message message) {
		logger.debug("OptometryClaimProcessor ReceivedMessage");

		try {
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder) object;

			if (validatePolicy(claimFolder)
					&& validateProcedure(claimFolder,
							ProcedureCategory.Optometry)) {
				Message claimMessage = getSession().createObjectMessage(
						claimFolder);
				paymentProducer.send(claimMessage);
			}
		} catch (Exception ex) {
			logError("OptometryClaimProcessor.onMessage() " + ex.getMessage(),
					ex);
		}

	}

}