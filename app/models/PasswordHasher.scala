package models

import com.google.inject.ImplementedBy
import java.security.SecureRandom
import org.apache.commons.codec.binary.Hex
import org.bouncycastle.crypto.PasswordConverter
import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.crypto.generators.SCrypt
import org.bouncycastle.crypto.prng.DigestRandomGenerator
import play.api.Logger

/**
  * Hashes and salts passwords.
  *
  * Password hashes MUST be salted, in other words it MUST be true that
  * `hash(plaintext) != hash(plaintext)`. Salts MUST be stored inline with the
  *  hash.
  */
@ImplementedBy(classOf[ScryptPasswordHasher])
trait PasswordHasher {
  def hash(plaintext: String): String
  def compare(hashed: String, plaintext: String): Boolean
}

/**
  * Hashes and salts passwords using Bouncy Castle's Scrypt implementation.
  */
class ScryptPasswordHasher extends PasswordHasher {
  private val logger      = Logger(getClass)
  private val pwConverter = PasswordConverter.UTF8
  private val rng         = new DigestRandomGenerator(new Blake2bDigest(512))
  logger.info("Seeding")
  rng.addSeedMaterial {
    val seeder = SecureRandom.getInstanceStrong
    val seed   = Array.ofDim[Byte](64)
    seeder.nextBytes(seed)
    seed
  }
  logger.info("Done seeding")

  private def newSalt: Array[Byte] = {
    val buf = Array.ofDim[Byte](32)
    rng.nextBytes(buf)
    buf
  }

  private def hashWithSalt(plaintext: String, salt: Array[Byte]): String = {
    val bytes = pwConverter.convert(plaintext.toCharArray())
    // These factors MUST match hash_password in datatruck.py,
    // or Datatruck migration WILL be broken
    val cipherText = SCrypt.generate(bytes, salt, 1 << 15, 8, 1, 64)
    Hex.encodeHexString(cipherText)
  }

  override def hash(plaintext: String): String = {
    val salt   = newSalt
    val cipher = hashWithSalt(plaintext, salt)
    s"${Hex.encodeHexString(salt)}&$cipher"
  }

  override def compare(hashed: String, plaintext: String): Boolean = {
    val Array(saltStr, cipherText) = hashed.split("&")
    val salt                       = Hex.decodeHex(saltStr)
    hashWithSalt(plaintext, salt) == cipherText
  }
}