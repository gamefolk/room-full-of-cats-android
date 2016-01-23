# Room Full of Cats

*Room Full of Cats* is a native game for Android and iOS devices.

## Compiling

Room Full of Cats is based on the [Absurd Engine], which cross-compiles its Android Java code to native iOS code.
To compile for Android, import the project into your IDE of choice and run as usual.
To compile for iOS, install the Absurd Engine and in the project root create a file `local.properties` with the following properties:

```
sdk.dir=[path of Android SDK]
xmlvm.sdk.jar=[path of XMLVM binary]

src.absurdengine.dir=[path of Absurd Engine]/src
src_native.absurdengine.dir=[path of Absurd Engine]/src_ios

src.java.dir=src
src.gen.dir=gen_ios
resource.dir=res

manifest.ios=androidmanifest_ios/AndroidManifest.xml
```

Finally, run `ant` in the project root - this will generate an Xcode project that you can run as usual.

## Notes

Music (/assets), graphics (/res/drawable), and API keys (/src/org/gamefolk/roomfullofcats/ApiKeys.java) are not included in this repo.
This repository is mirrored from my private Mercurial repo with the [Hg-Git] plugin.

[Absurd Engine]: https://bitbucket.org/smpsnr/absurdengine
[Hg-Git]: https://hg-git.github.io/
