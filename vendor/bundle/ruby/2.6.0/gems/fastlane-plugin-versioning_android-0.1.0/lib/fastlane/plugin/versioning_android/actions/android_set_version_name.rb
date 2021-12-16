module Fastlane
  module Actions
    module SharedValues
      ANDROID_NEW_VERSION_NAME = :ANDROID_NEW_VERSION_NAME
    end

    class AndroidSetVersionNameAction < Action
      def self.run(params)
        gradle_file_path = Helper::VersioningAndroidHelper.get_gradle_file_path(params[:gradle_file])
        new_version_name = Helper::VersioningAndroidHelper.get_new_version_name(gradle_file_path, params[:version_name])
        # bump_type ||= params[:bump_type]

        saved = Helper::VersioningAndroidHelper.save_key_to_gradle_file(gradle_file_path, "versionName", new_version_name)

        if saved == -1
          UI.user_error!("Unable to set the Version Name in build.gradle file at #{gradle_file_path}.")
        end

        UI.success("☝️  Android Version Name has been set to: #{new_version_name}")

        # Store the versionName in the shared hash
        Actions.lane_context[SharedValues::ANDROID_NEW_VERSION_NAME] = new_version_name
      end

      def self.description
        "Set the Version Name of your Android project"
      end

      def self.details
        "This action will set the new Version Name on your Android project."
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(key: :gradle_file,
                                  env_name: "FL_ANDROID_SET_VERSION_NAME_GRADLE_FILE",
                               description: "(optional) Specify the path to your app build.gradle if it isn't in the default location",
                                  optional: true,
                                      type: String,
                             default_value: "app/build.gradle",
                              verify_block: proc do |value|
                                UI.user_error!("Could not find app build.gradle file") unless File.exist?(value) || Helper.test?
                              end),
          FastlaneCore::ConfigItem.new(key: :version_name,
                                  env_name: "FL_ANDROID_SET_VERSION_NAME_VERSION_NAME",
                               description: "(optional) Set specific Version Name",
                                  optional: true,
                                      type: String,
                             default_value: nil),
          FastlaneCore::ConfigItem.new(key: :bump_type,
                                  env_name: "FL_ANDROID_SET_VERSION_NAME_BUMP_TYPE",
                               description: "(optional) Type of version bump (major, minor, patch)",
                                  optional: true,
                                      type: String,
                             default_value: nil)
        ]
      end

      def self.output
        [
          ['ANDROID_NEW_VERSION_NAME', 'The new Version Name of your Android project']
        ]
      end

      def self.return_value
        "The new Version Name of your Android project"
      end

      def self.authors
        ["Igor Lamoš"]
      end

      def self.is_supported?(platform)
        [:android].include? platform
      end

      def self.example_code
        [
          'android_set_version_name(
            version_name: "2.34.5" # Set a specific number
          )',
          'android_set_version_name(
            version_name: "2.34.5",
            gradle_file: "/path/to/build.gradle" # build.gradle is not in the default location
          )'
        ]
      end
    end
  end
end
