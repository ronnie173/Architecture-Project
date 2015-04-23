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

public class ProcessRadiologyClaim extends AbstractProcedureProcessor implements
		MessageListener {

	public ProcessRadiologyClaim(Session session) {
		super(session);
		// TODO Auto-generated constructor stub
	}

	private final static Logger logger = Logger
			.getLogger(ProcessRadiologyClaim.class);
	
	private MessageProducer producer;
    
	@Override
	public void onMessage(Message message) {
		logger.debug("RadiologyClaimProcessor ReceivedMessage");

		try {
			Object object = ((ObjectMessage) message).getObject();
			ClaimFolder claimFolder = (ClaimFolder) object;

			if (validatePolicy(claimFolder)
					&& validateProcedure(claimFolder,
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
				.createQueue(QueueNames.payClaim);
		producer = getSession().createProducer(queue);

	}

}
