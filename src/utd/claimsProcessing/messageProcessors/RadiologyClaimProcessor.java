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

public class RadiologyClaimProcessor extends MessageProcessor implements
		MessageListener {

	public RadiologyClaimProcessor(Session session) {
		super(session);
		// TODO Auto-generated constructor stub
	}

	private final static Logger logger = Logger
			.getLogger(RadiologyClaimProcessor.class);
	private AbstractProcedureProcessor processor;
	private MessageProducer producer;

	@Override
	public void onMessage(Message message) {
		logger.debug("RadiologyClaimProcessor ReceivedMessage");

		try {
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder) object;

			if (processor.validatePolicy(claimFolder)
					&& processor.validateProcedure(claimFolder,
							ProcedureCategory.Radiology)) {
				Message claimMessage = getSession().createObjectMessage(
						claimFolder);
				producer.send(claimMessage);
			}
		} catch (Exception ex) {
			logError("RadiologyClaimProcessor.onMessage() " + ex.getMessage(),
					ex);
		}

	}

	@Override
	public void initialize() throws JMSException {
		Queue queue = getSession()
				.createQueue(QueueNames.processRadiologyClaim);
		producer = getSession().createProducer(queue);

	}

}
