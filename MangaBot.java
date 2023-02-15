import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

public class MangaBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/help")) {
                sendTextMessage(chatId, "Daftar perintah yang tersedia:\n/manga <query> - untuk mencari manga dari https://komikindo.id");
            } else if (messageText.startsWith("/manga")) {
                String query = messageText.substring(7).replaceAll(" ", "+");
                sendManga(chatId, query);
            }
        }
    }

    private void sendTextMessage(long chatId, String text) {
        try {
            execute(new SendMessage(chatId, text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendManga(long chatId, String query) {
        try {
            Document doc = Jsoup.connect("https://komikindo.id/?s=" + query + "&post_type=manga").get();
            Elements mangaList = doc.select(".tab-thumb > a");
            if (mangaList.isEmpty()) {
                sendTextMessage(chatId, "Maaf, tidak ditemukan manga dengan judul tersebut.");
            } else {
                for (Element manga : mangaList) {
                    String title = manga.select(".tab-thumb-title").text();
                    String imgUrl = manga.select("img").attr("src");
                    sendPhoto(chatId, title, imgUrl);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPhoto(long chatId, String caption, String url) {
        new Thread(() -> {
            try {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setCaption(caption);
                sendPhoto.setPhoto(url);
                execute(sendPhoto);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public String getBotUsername() {
        return "NamaBotAnda";
    }

    @Override
    public String getBotToken() {
        return "TokenBotAnda";
    }
}
