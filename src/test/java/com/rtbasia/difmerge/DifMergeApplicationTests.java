package com.rtbasia.difmerge;

import com.rtbasia.difmerge.ipfs.IPFSClient;
import io.ipfs.api.MerkleNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DifMergeApplicationTests {

    @Autowired
    @Qualifier(value="localIpfs")
    private IPFSClient localClient;

    @Autowired
    @Qualifier(value="remoteIpfs")
    private IPFSClient remoteClient;

	@Test
	public void contextLoads() {
	}

    @Test
	public void testUploadFile() throws IOException, TimeoutException {
        byte[] largerData = new byte[1*1024*1024];

        new Random(1).nextBytes(largerData);

        MerkleNode node = localClient.upload(largerData);

        byte[] downloadData = localClient.cat(node.hash.toBase58());

        Assert.assertTrue(Arrays.equals(downloadData, largerData));
    }

    @Test
    public void testLsFileNotExist() throws IOException {
        String base58hash = "QmXsRoUDJcxFy1QSRKAkrnUf67BmZ8TPWDrAxrqKY757Pj"; // this hash not exists

        boolean exists = localClient.fileExists(base58hash);

        Assert.assertFalse(exists);
    }

    @Test
    public void testLsFileExist() throws IOException {
        MerkleNode node  = localClient.upload("IPFS is awesome!!!");

        boolean exists = localClient.fileExists(node.hash.toBase58());

        Assert.assertTrue(exists);
    }

    @Test
    public void testStat() throws IOException {
        MerkleNode node = remoteClient.upload(UUID.randomUUID().toString());

        Map<String, Object> result = localClient.stat(node.hash.toBase58());

        Assert.assertTrue(result.size() > 0);
    }
}
