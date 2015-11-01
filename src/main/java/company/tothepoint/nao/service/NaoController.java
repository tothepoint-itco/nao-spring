package company.tothepoint.nao.service;

import com.aldebaran.qi.CallError;
import com.aldebaran.qi.helper.EventCallback;
import com.aldebaran.qi.helper.proxies.*;
import company.tothepoint.nao.service.picture.Picture;
import company.tothepoint.nao.service.picture.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/nao")
public class NaoController {
    private static final Logger LOG = LoggerFactory.getLogger(NaoController.class);


    private static ALTextToSpeech tts;
    private static ALMotion motion;
    private static ALMemory memory;
    private static ALVideoDevice video;
    private static ALAudioPlayer audio;
    private static ALSpeechRecognition speechRecognition;

    private static String moduleName;

    @Autowired
    private RobotSession robotSession;


    /**
     * Camera settings
     */
    static int topCamera = 0;
    static int resolution = 2; // 640 x 480
    static int colorspace = 11; // RGB
    static int frameRate = 10; // FPS


    EventCallback<Float> eventCallback1 = new EventCallback<Float>() {
        @Override
        public void onEvent(Float aFloat) throws InterruptedException, CallError {
            if (aFloat > 0) {
                try {
                    tts = new ALTextToSpeech(robotSession.getSession());
                    tts.say("You touched me!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    public NaoController() {
        try {
            tts = new ALTextToSpeech(robotSession.getSession());
            memory = new ALMemory(robotSession.getSession());

            memory.subscribeToEvent("RearTactilTouched", new EventCallback<Float>() {
                @Override
                public void onEvent(Float arg0) throws InterruptedException, CallError {
                    if (arg0 > 0) {
                        tts.say("I felt that!");
                    }
                }
            });
            memory.subscribeToEvent("FrontTactilTouched", eventCallback1);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }


    @RequestMapping(method = RequestMethod.GET, value="/testreacttovoice")
    public void testVoice() {
        try {
            LOG.debug("Method testVoice() called!");
            tts = new ALTextToSpeech(robotSession.getSession());
            memory = new ALMemory(robotSession.getSession());
            speechRecognition = new ALSpeechRecognition(robotSession.getSession());
            speechRecognition.pause(true);
            ArrayList<String> listOfWords = new ArrayList<String>();
            listOfWords.add("To The Point");
            listOfWords.add("Twitter");
            listOfWords.add("Martin Fowler");
            listOfWords.add("Tweet");
            listOfWords.add("Read tweets");
            speechRecognition.setVocabulary(listOfWords, false);
            speechRecognition.pause(false);
            speechRecognition.subscribe("voiceTest");

            memory.subscribeToEvent("WordRecognized", new EventCallback<List<Object>>() {
                @Override
                public void onEvent(List<Object> words) throws InterruptedException, CallError {
                    String word = (String) words.get(0);
                    LOG.info("Word " + word);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(method = RequestMethod.GET, value="/readtweets")
    ResponseEntity<List<String>> readTweets() {
        LOG.debug("Method readTweets() called!");
        try {
            tts = new ALTextToSpeech(robotSession.getSession());
            TwitterService twitterService = new TwitterService();
            List<String> tweets = twitterService.getTweetsFrom("martinfowler");

            for (String tweet: tweets) {
                tts.say("Tweet: ");
                tts.say(tweet);
            }
            return new ResponseEntity<List<String>>(tweets, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value="/picture")
    public Boolean takePicture() {
        LOG.debug("Method takePicture() called!");
        try {
            audio = new ALAudioPlayer(robotSession.getSession());
            motion = new ALMotion(robotSession.getSession());
            video = new ALVideoDevice(robotSession.getSession());
            moduleName = video.subscribeCamera("TakePicture", topCamera, resolution, colorspace, frameRate);
            LOG.warn("subscribed with id: " + moduleName);
            motion.wakeUp();
            @SuppressWarnings("unchecked")
            List<Object> image = (List<Object>) video.getImageRemote(moduleName);
            ByteBuffer buffer = (ByteBuffer) image.get(6);
            byte[] rawData = buffer.array();
            Picture p = Util.toPicture(rawData);
            video.unsubscribe(moduleName);
            tts.say("I just took a picture!");
            return true;

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    @RequestMapping(method = RequestMethod.GET, value="/say/{s}")
    public ResponseEntity<String> saySomething(@PathVariable String s) {
        LOG.debug("Method saySomething() called!");
        try {
            LOG.debug("saySomething on endpoint /say/"+s+" called");
            tts = new ALTextToSpeech(robotSession.getSession());
            tts.say(s);
            return new ResponseEntity<String>("I just said: "+s, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }





    @RequestMapping(method = RequestMethod.GET, value="/move/forward")
    public ResponseEntity<String> moveForward() {
        LOG.debug("Method moveForward() called!");
        try {
            motion = new ALMotion(robotSession.getSession());
            tts = new ALTextToSpeech(robotSession.getSession());
            tts.say("Careful! I'm coming to get you!");
            motion.moveTo(1f, 0f, 0f);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }
    @RequestMapping(method = RequestMethod.GET, value="/move/left")
    public ResponseEntity<String> moveLeft() {
        LOG.debug("Method moveLeft() called!");
        try {
            motion = new ALMotion(robotSession.getSession());
            tts = new ALTextToSpeech(robotSession.getSession());
            motion.moveTo(0f, -1f, 0f, 1.0);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }
    @RequestMapping(method = RequestMethod.GET, value="/move/right")
    public ResponseEntity<String> moveRight() {
        LOG.debug("Method moveRight() called!");
        try {
            motion = new ALMotion(robotSession.getSession());
            tts = new ALTextToSpeech(robotSession.getSession());
            motion.moveTo(0f, 1f, 0f, 1.0);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }
    @RequestMapping(method = RequestMethod.GET, value="/move/backward")
    public ResponseEntity<String> moveBackward() {
        LOG.debug("Method moveBackward() called!");
        try {
            motion = new ALMotion(robotSession.getSession());
            tts = new ALTextToSpeech(robotSession.getSession());
            motion.moveTo(1f, 0f, 0f);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }
}