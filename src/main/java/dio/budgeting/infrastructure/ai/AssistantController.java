package dio.budgeting.infrastructure.ai;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final VoiceAssistant voiceAssistant;

    public AssistantController(VoiceAssistant voiceAssistant) {
        this.voiceAssistant = voiceAssistant;
    }

    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "audio/mpeg")
    public ResponseEntity<Resource> processVoice(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de áudio não pode estar vazio");
        }

        var result = voiceAssistant.process(file.getResource());
        var resource = new ByteArrayResource(result.audio());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .contentLength(result.audio().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("assistant-response.mp3").build().toString())
                .body(resource);
    }
}
