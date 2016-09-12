package org.glytoucan.api.controller;

import static org.junit.Assert.assertEquals;

import org.glytoucan.api.Application;
import org.glytoucan.api.controller.GlycoSequenceController;
import org.glytoucan.model.GlycoSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class GlycoSequenceControllerTest {
	public static Logger logger = (Logger) LoggerFactory
			.getLogger("org.glytoucan.ws.controller.GlycoSequenceControllerTest");

	@Autowired
	GlycoSequenceController glycoSequenceController;

	@Test
	@Transactional
	public void testRetrieve() throws Exception { 
		GlycoSequence gs = glycoSequenceController.retrieve("G00030MO");
		logger.debug(gs.getSequence() + gs.getPrimaryId());
		assertEquals("G00030MO", gs.getPrimaryId());
		assertEquals("WURCS=2.0/4,7,6/[u2122h_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5]/1-2-3-4-2-4-2/a4-b1_b4-c1_c3-d1_c6-f1_e1-d2|d4_g1-f2|f4", gs.getSequence());
	}
	
//	@Test
//	public void testRetrieveMotif() throws Exception {
//		GlycoSequence gs = glycoSequenceController.retrieveMotif();
//		logger.debug(gs.getSequence() + gs.getPrimaryId());
//	}
}