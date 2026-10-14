package dio.budgeting.infrastructure.http;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.Charset;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final ChatClient chatClient;

    public TransactionController(
            PersistTransactionUseCase persistTransactionUseCase,
            ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
            @Value("classpath:prompts/system-message.st") Resource systemPrompt,
            ChatClient.Builder chatClientBuilder) throws IOException {

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt.getContentAsString(Charset.defaultCharset()))
                .defaultTools(persistTransactionUseCase, listTransactionsByCategoryUseCase)
                .build();
    }

    @PostMapping
    public ResponseEntity<String> handleMessage(@RequestBody TransactionRequest request) {
        // Envia a descrição da transação como prompt para o ChatClient utilizar as tools
        String chatResponse = chatClient.prompt()
                .user(request.description())
                .call()
                .content();

        return ResponseEntity.ok(chatResponse);
    }
}