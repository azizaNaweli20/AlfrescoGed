package com.xtensus.xteged.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xtensus.xteged.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Test class for the GEDControllerResource REST controller.
 *
 * @see GEDControllerResource
 */
@IntegrationTest
class GEDControllerResourceIT {

    private MockMvc restMockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        GEDControllerResource gEDControllerResource = new GEDControllerResource();
        restMockMvc = MockMvcBuilders.standaloneSetup(gEDControllerResource).build();
    }

    /**
     * Test uploadDocument
     */
    @Test
    void testUploadDocument() throws Exception {
        restMockMvc.perform(post("/api/ged-controller/upload-document")).andExpect(status().isOk());
    }
}
