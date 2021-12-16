module Fastlane
  module Actions
    class SentryUploadProguardAction < Action
      def self.run(params)
        Helper::SentryHelper.check_sentry_cli!
        Helper::SentryConfig.parse_api_params(params)

        # Params - mapping & manifest
        mapping_path = params[:mapping_path]
        android_manifest_path = params[:android_manifest_path]

        # Verify files
        UI.user_error!("Mapping file does not exist at path: #{mapping_path}") unless File.exist? mapping_path
        UI.user_error!("AndroidManifest.xml file does not exist at path: #{android_manifest_path}") unless File.exist? android_manifest_path

        command = [
          "sentry-cli",
          "upload-proguard",
          "--android-manifest",
          android_manifest_path,
          mapping_path
        ]

        Helper::SentryHelper.call_sentry_cli(command)
        UI.success("Successfully uploaded mapping file!")
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Upload mapping to a project on Sentry"
      end

      def self.details
        [
          "This action allows you to upload the proguard mapping file to Sentry.",
          "See https://docs.sentry.io/cli/dif/proguard for more information."
        ].join(" ")
      end

      def self.available_options
        Helper::SentryConfig.common_api_config_items + [
          FastlaneCore::ConfigItem.new(key: :mapping_path,
                                      env_name: "ANDROID_MAPPING_PATH",
                                      description: "Path to your proguard mapping.txt file",
                                      optional: false,
                                      verify_block: proc do |value|
                                        UI.user_error! "Could not find your mapping file at path '#{value}'" unless File.exist?(value)
                                      end),
          FastlaneCore::ConfigItem.new(key: :android_manifest_path,
                                      env_name: "ANDROID_MANIFEST_PATH",
                                      description: "Path to your merged AndroidManifest file. This is usually found under `app/build/intermediates/manifests/full`",
                                      optional: false,
                                      verify_block: proc do |value|
                                        UI.user_error! "Could not find your merged AndroidManifest file at path '#{value}'" unless File.exist?(value)
                                      end)
        ]
      end

      def self.return_value
        nil
      end

      def self.authors
        ["mpp-anasa"]
      end

      def self.is_supported?(platform)
        platform == :android
      end
    end
  end
end
