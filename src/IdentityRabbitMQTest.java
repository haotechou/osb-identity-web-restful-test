/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Haote Chou
 */
public class IdentityRabbitMQTest {

	public static void main(String[] args) throws Exception {
//		File file = new File(
//			new File("").getAbsolutePath() + "/resources/users.csv");

		File file = new File(
			new File("").getAbsolutePath() + "/../resources/users.csv");

		if (file.exists()) {
			BufferedReader bufferedReader =
				new BufferedReader(new FileReader(file));

			int counter = 1;
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				Date startTime = new Date();

				setReplyQueueName();

				String responseMessage = call(line);

				Date endTime = new Date();

				long difference = endTime.getTime() - startTime.getTime();

				System.out.println(
					"Entry " + counter + " spent " + difference + "ms");

//				printMessage(counter, responseMessage);

				close();

				counter++;
			}

			bufferedReader.close();
		}
		else {
			System.out.println("users.csv does not exist");
		}
	}

	protected static String call(String message)
		throws IOException, InterruptedException {

		final String corrId = UUID.randomUUID().toString();

		AMQP.BasicProperties.Builder builderHolder =
			new AMQP.BasicProperties.Builder();

		AMQP.BasicProperties.Builder builder =
			builderHolder.correlationId(corrId);

		AMQP.BasicProperties.Builder replyBuilder = builder.replyTo(
			replyQueueName);

		AMQP.BasicProperties props = replyBuilder.build();

		channel.basicPublish(
			"", "IDENTITY_QUEUE", props, message.getBytes("UTF-8"));

		final BlockingQueue<String> response =
			new ArrayBlockingQueue<String>(1);

		channel.basicConsume(
			replyQueueName, true, new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(
					String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body)
				throws IOException {

				if (properties.getCorrelationId().equals(corrId)) {
					response.offer(new String(body, "UTF-8"));
				}
			}
		});

		return response.take();
	}

	protected static void close() throws IOException {
		connection.close();
	}

	protected static void printMessage(int counter, String line)
		throws Exception {

		JSONObject outerJSONObject = new JSONObject(line);

		JSONArray jsonArray = outerJSONObject.getJSONArray("users");

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject innerJSONObject = jsonArray.getJSONObject(i);

			String message = "Entry " + counter + ": " +
				innerJSONObject.get("userId") + ", " +
				innerJSONObject.get("firstName") + ", " +
				innerJSONObject.get("lastName") + ", " +
				innerJSONObject.get("fullName") + ", " +
				innerJSONObject.get("emailAddress");

			System.out.println(message);
		}
	}

	protected static void setReplyQueueName() throws Exception {
		ConnectionFactory connectionFactory = new ConnectionFactory();

		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setHost("cloud-10-50-0-214");
		connectionFactory.setPassword("85hJEYp46l6t5tX");
		connectionFactory.setPort(5672);
		connectionFactory.setUsername("haote.chou");

		connection = connectionFactory.newConnection();

		channel = connection.createChannel();

		replyQueueName = channel.queueDeclare().getQueue();
	}

	private static Channel channel;
	private static Connection connection;
	private static String replyQueueName;

}