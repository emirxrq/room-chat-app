## Room Chat App (Odalarla Sohbet Uygulaması)

Herkese merhabalar ben Emir. Javaya yeni başladım. Bir çok programlama dilinde Socketler ile uğraştım ve bugünde Javada Socket kullanarak mini bir sohbet uygulaması geliştirdim. Bu uygulamanın çok çok basit olduğunu ve sadece öğretim amaçlı yapıldığını baştan söylemek istiyorum. Tabikide kullanılabilir bir sohbet uygulaması değil ancak içerisindeki kodlar işinize yarayabilir. Ben sadece kendimi bu alanda geliştirmek ve az çok birşey öğrenmek istiyenler için böyle bir proje geliştirdim. Projede çok fazla eksik var örneğin mesajların veritabanına kaydedilmemesi, kayıt ve giriş sistemi olmaması sadece isim ile sunucuya bağlanılabilmesi gibi... Ama dediğim gibi sadece Socketi öğrenme amaçlı yaptığım bir projeydi. İlerde daha gelişmiş DM sistemli, arkadaş eklemeli, veritabanında mesajların kaydedilmeli, kayıt ve giriş sistemlerinin olduğu bir sohbet uygulaması geliştirebilirim belki. 

## Room Chat App nedir?
Burada herkes kendi odasını oluşturabiliyor ve Socket sayesinde herşey gerçek zamanlı oluyor. Biri yeni oda açtığında bu herkeste anında güncelleniyor. Mesaj göndermeleriniz anında ve hızlı oluyor. Odaları veritabanında kaydettiriyoruz ve her uygulama açıldığında odalar yükleniyor. Hangi odaya bağlandıysan sadece o odanın mesajlarını alıyorsun. Ve odaya çıkış - giriş mesajları ekledim.

## Nasıl kurabiliriz?

İlk öncelikle şunu söylemem lazım projede JavaFX, MySQL Connector gibi bağımlılıklar ekledim. Zaten proje maven bir proje. Burada sıfırdan yazamam ancak YouTubeden Eclipse Maven JavaFX nasıl kurulur gibi bir kaç video ile öğrenebilirsiniz.

MySQL'in hata vermemesi için: https://www.youtube.com/watch?v=_WrEwxVBZ8M

Eclipsede JavaFX ve SceneBuilder kurmak için: https://www.youtube.com/watch?v=Dbb69NiQHso

Son olarak MySQL'i kurmak için projede .sql dosyasını zaten bıraktım, MySQL'de onu import edebilirsiniz.

![Örnek Resim](example.png)
